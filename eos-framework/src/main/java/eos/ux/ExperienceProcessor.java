package eos.ux;

import com.sun.net.httpserver.HttpExchange;
import eos.exception.EosException;
import eos.model.web.*;
import eos.model.web.Iterable;
import eos.web.Fragment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExperienceProcessor {

    final String NEWLINE = "\n";
    final String FOREACH = "<eos:each";
    final String IFSPEC = "<eos:if spec=";
    final String ENDIF = "</eos:if";
    final String DATA = "<eos:set var=";


    public String process(Map<String, Fragment> pointcuts, String view, HttpResponse httpResponse, HttpRequest request, HttpExchange exchange) throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        List<String> entries = Arrays.asList(view.split("\n"));
        List<IterablePartial> iterablePartials = new ArrayList<>();
        List<DataPartial> dataPartials = new ArrayList<>();
        List<SpecPartial> specPartials = new ArrayList<>();

        for(int line = 0; line < entries.size(); line++){
            String entry = entries.get(line);

            if(entry.contains(this.DATA)) {
                DataPartial dataPartial = new DataPartial();
                dataPartial.setEntry(entry);
                dataPartial.setIterable(false);
                if(insideIterable(line, iterablePartials)){
                    dataPartial.setIterable(true);
                }
                dataPartials.add(dataPartial);
            }else if(entry.contains(this.FOREACH) && !insideIterable(line, iterablePartials)){

                IterablePartial iterablePartial = new IterablePartial();
                Iterable iterable = getIterable(line, entry, httpResponse, entries);
                iterablePartial.setEntries(iterable.getEntries());
                iterablePartial.setIterable(iterable);
                iterablePartials.add(iterablePartial);

            }else if(entry.contains(this.IFSPEC)){

                SpecPartial specPartial = new SpecPartial();
                StopGo stopGo = getSpecStopGo(line, entries);
                specPartial.setStopGo(stopGo);
                List<String> specEntries = getSpecEntries(line, entries);
                specPartial.setEntries(specEntries);

                specPartial.setIterable(false);
                if(insideIterable(line, iterablePartials)){
                    specPartial.setIterable(true);
                }
                specPartials.add(specPartial);
            }
        }

        List<StopGo> specStopGos = new ArrayList<>();
        for(int foo = 0; foo < specPartials.size(); foo++){
            SpecPartial specPartial = specPartials.get(foo);
            List<String> specEntries = specPartial.getEntries();
            int go = specPartial.getStopGo().getGo();
            String specEntry = specEntries.get(go);
            StopGo stopGo = evaluate(go, specEntry, httpResponse, specEntries);
            if(stopGo != null) {
                specStopGos.add(stopGo);
            }
        }

        /**
         * who ever is saying stop is connected to the richmonds, they have the birds eye view
         */
        StringBuilder z = new StringBuilder();



        return z.toString();
    }


    public List<String> evaluateForEach(HttpResponse resp, List<StopGo> specStopGos, List<IterablePartial> iterablePartials) throws InvocationTargetException, NoSuchMethodException, EosException, IllegalAccessException, NoSuchFieldException {
        List<String> combined = new ArrayList();
        for(int foo = 0; foo < iterablePartials.size(); foo++){
            IterablePartial iterablePartial = iterablePartials.get(foo);
            for(int baz = iterablePartial.getIterable().getGo(); baz < iterablePartial.getIterable().getStop(); baz++){
                System.out.println(iterablePartial.getIterable().getEntries().get(baz));
            }
            if(exercisePartial(iterablePartial, specStopGos)){
                List<String> iterableEntries = iterablePartial.getEntries();
                int stop = iterablePartial.getIterable().getStop();
                int go = iterablePartial.getIterable().getGo();

                List<IterablePartial> deepIterablePartials = new ArrayList<>();
                List<StopGo> deepSpecStopGos = new ArrayList<>();

                for(int baz = go; baz < stop; baz++){
                    String entry = iterablePartial.getEntries().get(baz);
                    String activeField = iterablePartial.getIterable().getField();
                    if(entry.contains(this.DATA))setVariable(entry, resp);

                    if(entry.contains(this.IFSPEC)){
                        List<SpecPartial> specPartials = getSpecPartials(iterableEntries);
                        for(int bar = 0; bar < specPartials.size(); bar++){
                            SpecPartial specPartial = specPartials.get(bar);
                            List<String> specEntries = specPartial.getEntries();
                            StopGo stopGo = evaluate(go, entry, resp, specEntries);
                            if(stopGo != null) {
                                deepSpecStopGos.add(stopGo);
                            }
                        }
                    }

                    if(entry.contains(this.FOREACH)){
                        IterablePartial deepIterablePartial = new IterablePartial();
                        Iterable iterable = getIterableDeep(baz, entry, resp, iterableEntries);
                        iterablePartial.setEntries(iterable.getEntries());
                        iterablePartial.setIterable(iterable);
                        deepIterablePartials.add(deepIterablePartial);
                        evaluateForEach(resp, deepSpecStopGos, deepIterablePartials);
                    }

                    String hydratedLine = evaluateEntry(baz, 0, activeField, entry, resp);

                }

            }
        }
        return combined;
    }





    public boolean exercisePartial(IterablePartial partial, List<StopGo> specStopGos){
        for(int foo = 0; foo < specStopGos.size(); foo++){
            StopGo stopGo = specStopGos.get(foo);
            Iterable iterable = partial.getIterable();
            int iterableStop = iterable.getStop();
            int iterableGo = iterable.getGo();

            if(iterableGo > stopGo.getGo() && iterableStop < iterable.getStop()){
                return false;
            }
        }
        return true;
    }


    public StopGo getSpecStopGo(int line, List<String> entries){
        StopGo stopGo = new StopGo();
        int stop = getSpecStop(line, entries);
        stopGo.setStop(stop);
        stopGo.setGo(line);
        return stopGo;
    }



    public List<String> getSpecEntries(int line, List<String> entries){
        int stop = getSpecStop(line, entries);
        List<String> specEntries = new ArrayList<>();
        for(int foo = line; foo < stop; foo++){
            String entry = entries.get(foo);
            specEntries.add(entry);
        }
        return specEntries;
    }

    public int getSpecStop(int line, List<String> entries){
        int startCount = 1, endCount = 0;
        for(int baz = line + 1; baz < entries.size(); baz++){
            String entry = entries.get(baz);
            if(entry.contains(this.ENDIF)){
                endCount++;
            }
            if(entry.contains(this.IFSPEC)){
                startCount++;
            }
            if(startCount == endCount &&
                    entry.contains(this.ENDIF)){
                return baz + 1;
            }
        }
        return line;
    }


    public Boolean insideIterable(int lineNumber, List<IterablePartial> iterablePartials){
        for(int waldo = 0; waldo < iterablePartials.size(); waldo++){
            IterablePartial iterablePartial = iterablePartials.get(waldo);
            if(lineNumber > iterablePartial.getIterable().getGo() &&
                    lineNumber < iterablePartial.getIterable().getStop()){
                return true;
            }
        }
        return false;
    }


    private StringBuilder retrieveFinal(StringBuilder eachOut){
        StringBuilder finalOut = new StringBuilder();
        String[] parts = eachOut.toString().split("\n");
        for(String bit : parts){
            if(!bit.trim().equals(""))finalOut.append(bit + this.NEWLINE);
        }
        return finalOut;
    }

    private void iterateEvaluate(int a8, StringBuilder deepEachOut, Iterable iterable, HttpResponse httpResponse, List<String> entries) throws NoSuchFieldException, IllegalAccessException, EosException, NoSuchMethodException, InvocationTargetException {
        for(int a7 = 0; a7 < iterable.getPojos().size(); a7++) {
            Object obj = iterable.getPojos().get(a7);
            List<Integer> ignore = new ArrayList<>();
            for (int a6 = iterable.getGo(); a6 < iterable.getStop(); a6++) {
                String entry = entries.get(a6);
                if(entry.contains("<eos:if spec=")){
                    ignore = evaluateEachCondition(a8, entry, obj, iterable, httpResponse, entries);
                }
                if(ignore.contains(a8))continue;
                if (entry.contains(this.FOREACH)) continue;
                entry = evaluateEntry(0, 0, iterable.getField(), entry, httpResponse);
                if(entry.contains("<eos:set")){
                    setEachVariable(entry, httpResponse, obj);
                }
                System.out.println(entry);
                evaluateEachEntry(entry, deepEachOut, obj, iterable.getField());
            }
        }
    }

    private List<Integer> evaluateEachCondition(int a8, String entry, Object obj, Iterable iterable, HttpResponse httpResponse, List<String> entries) throws NoSuchFieldException, IllegalAccessException {
        List<Integer> ignore = new ArrayList<>();

        int stop = getEachConditionStop(a8, entries);
        int startIf = entry.indexOf("<eos:if spec=");

        int startExpression = entry.indexOf("${", startIf);
        int endExpression = entry.indexOf("}", startExpression);

        String expression = entry.substring(startExpression + 2, endExpression);

        String condition = getCondition(expression);
        String[] bits = expression.split(condition);

        String subjectPre = bits[0].trim();
        String predicatePre = bits[1].trim();

        //<eos:if spec="${town.id == organization.townId}">
        if(subjectPre.contains(".")) {

            int startSubject = subjectPre.indexOf(".");
            String subjectKey = subjectPre.substring(startSubject + 1).trim();


            int firstNotation = subjectPre.indexOf(".");
            String field = subjectPre.substring(0, firstNotation);
            if (field.equals(iterable.getField())) {

                Object subjectObj = getValueRecursive(0, subjectKey, obj);
                String subject = String.valueOf(subjectObj);
                System.out.println("z: " + subject + ":" + subjectKey + ":" + obj);

                if (predicatePre.equals("null")) {

                    if (subjectObj == null && condition.equals("!=")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }
                    if (subjectObj != null && condition.equals("==")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }

                } else if (predicatePre.contains(".")) {

                    String[] predicateKeys = predicatePre.split("\\.");
                    String key = predicateKeys[0];

                    int startField = predicatePre.indexOf(".");
                    String passiton = predicatePre.substring(startField + 1);

                    Object keyObj = httpResponse.get(key);
                    Object value = getValueRecursive(0, passiton, keyObj);

                    String predicate = String.valueOf(value);

                    if (predicate.equals(subject) && condition.equals("!=")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }
                    if (!predicate.equals(subject) && condition.equals("==")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }

                } else if (!predicatePre.contains("'")) {
                    if (predicatePre.equals(subject) && condition.equals("!=")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }
                    if (!predicatePre.equals(subject) && condition.equals("==")) {
                        ignore = getIgnoreEntries(a8, stop);
                    }
                } else if (predicatePre.contains("'")) {
                    if (predicatePre.contains("''")) {

                        if (subject.equals("")) {
                            ignore = getIgnoreEntries(a8, stop);
                        } else {

                        }

                        subject = "'" + subject + "'";
                        if(!predicatePre.equals(subject) && condition.equals("==")){
                            ignore = getIgnoreEntries(a8, stop);
                        }
                        if(predicatePre.equals(subject) && condition.equals("!=")){
                            ignore = getIgnoreEntries(a8, stop);
                        }
                    } else {
                        String predicate = predicatePre.replaceAll("'", "");
                        if (predicate.equals(subject) && condition.equals("!=")) {
                            ignore = getIgnoreEntries(a8, stop);
                        }
                        if (!predicate.equals(subject) && condition.equals("==")) {
                            ignore = getIgnoreEntries(a8, stop);
                        }
                    }
                }
            }

        }

        return ignore;
    }

    private void setVariable(String entry, HttpResponse httpResponse) throws NoSuchFieldException, IllegalAccessException {
        int startVariable = entry.indexOf("variable=\"");
        int endVariable = entry.indexOf("\"", startVariable + 10);
        //music.
        String variableKey = entry.substring(startVariable + 10, endVariable);

        int startValue = entry.indexOf("value=\"");
        int endValue = entry.indexOf("\"", startValue + 7);

        String valueKey;
        if(entry.contains("value=\"${")){
            valueKey = entry.substring(startValue + 9, endValue);
        }else{
            valueKey = entry.substring(startValue + 7, endValue);
        }

        if(valueKey.contains(".")){
            valueKey = valueKey.replace("}", "");
            String[] keys = valueKey.split("\\.");
            String key = keys[0];
            if(httpResponse.data().containsKey(key)) {
                Object obj = httpResponse.get(key);
                String field = keys[1];
                Field fieldObj = obj.getClass().getDeclaredField(field);
                fieldObj.setAccessible(true);
                Object valueObj = fieldObj.get(obj);
                String value = String.valueOf(valueObj);
                httpResponse.set(variableKey, value);
            }
        }else{
            httpResponse.set(variableKey, valueKey);
        }

    }


    private void setEachVariable(String entry, HttpResponse httpResponse, Object obj) throws NoSuchFieldException, IllegalAccessException {
        int startVariable = entry.indexOf("variable=\"");
        int endVariable = entry.indexOf("\"", startVariable + 10);

        String variableKey = entry.substring(startVariable + 10, endVariable);

        int startValue = entry.indexOf("value=\"");
        int endValue = entry.indexOf("\"", startValue + 7);

        String valueKey;
        if(entry.contains("value=\"${")){
            valueKey = entry.substring(startValue + 9, endValue);
        }else{
            valueKey = entry.substring(startValue + 7, endValue);
        }

        if(valueKey.contains(".")){
            Object value = getValueRecursive(0, valueKey, obj);
            httpResponse.set(variableKey, String.valueOf(value));
        }else{
            httpResponse.set(variableKey, valueKey);
        }

    }

    private void evaluatePointcuts(HttpRequest request, HttpExchange exchange, List<String> entries, Map<String, Fragment> pointcuts) {

        for(Map.Entry<String, Fragment> entry: pointcuts.entrySet()){
            Fragment fragment = entry.getValue();
            String key = fragment.getKey();//dice:rollem in <dice:rollem> is key

            String open = "<" + key + ">";
            String rabbleDos = "<" + key + "/>";
            String close = "</" + key + ">";
            for(int a6 = 0; a6 < entries.size(); a6++) {
                String entryBase = entries.get(a6);

                if(entryBase.trim().startsWith("<!--"))entries.set(a6, "");
                if(entryBase.trim().startsWith("<%--"))entries.set(a6, "");

                if(entryBase.contains(rabbleDos) &&
                        !fragment.isEvaluation()){
                    String output = fragment.process(request, exchange);
                    if(output != null) {
                        entryBase = entryBase.replace(rabbleDos, output);
                        entries.set(a6, entryBase);
                    }
                }

                if(entryBase.contains(open)){
                    int stop = getAttributeClose(a6, close, entries);
                    if(fragment.isEvaluation()){
                        Boolean isTrue = fragment.evaluatesTrue(request, exchange);
                        if(!isTrue){
                            for(int a4 = a6; a4 < stop; a4++){
                                entries.set(a4, "");
                            }
                        }
                    }
                    if(!fragment.isEvaluation()){
                        String output = fragment.process(request, exchange);
                        if(output != null) {
                            entryBase = entryBase.replace(open, output);
                            entryBase = entryBase.replace(open + close, output);
                            entries.set(a6, entryBase);
                            for (int a4 = a6 + 1; a4 < stop; a4++) {
                                entries.set(a4, "");
                            }
                        }
                    }
                }
            }
        }
    }

    private int getAttributeClose(int a6, String closeKey, List<String> entries) {
        for(int a5 = a6; a5 < entries.size(); a5++) {
            String entry = entries.get(a5);
            if(entry.contains(closeKey)){
                return a5;
            }
        }
        return a6;
    }

    private StopGo evaluate(int line, String entry, HttpResponse httpResponse, List<String> entries) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, EosException, NoSuchFieldException {

        int stop = getConditionStop(line, entries);
        StopGo stopGo = new StopGo();
        stopGo.setGo(line);
        stopGo.setStop(stop);

        System.out.println("evaluate > " + line + " : " + entry);

        int startIf = entry.indexOf("<eos:if spec=");

        int startExpression = entry.indexOf("${", startIf);
        int endExpression = entry.indexOf("}", startExpression);


        String expression = entry.substring(startExpression + 2, endExpression);

        String condition = getCondition(expression);
        String[] parts;
        if(!condition.equals("")) {//check if condition exists
            parts = expression.split(condition);
        }else{
            parts = new String[]{ expression };
        }

        if(parts.length > 1) {
            String left = parts[0].trim();

            if(left.contains(".")){
                String predicate = parts[1].trim();

                String[] keys = left.split("\\.");
                String objName = keys[0];
                int queryStart = left.indexOf(".");
                String query = left.substring(queryStart + 1);

                if(!keys[1].contains("(")){
                    String field = keys[1].trim();
                    if (httpResponse.data().containsKey(objName)) {
                        if(predicate.contains("'")) predicate = predicate.replace("'", "");
                        if (predicate != null && predicate != "") {
                            Object obj = httpResponse.get(objName);
                            String value = getValueRecursive(0, query, obj).toString();
                            checkCondition(line, stop, value, condition, predicate, entries);
                        }
                    } else {
                        return stopGo;
                    }

                }else{
                    String methodName = keys[1]
                            .replace("(", "")
                            .replace(")", "");

                    if (httpResponse.data().containsKey(objName)) {
                        Object obj = httpResponse.get(objName);
                        Method method = obj.getClass().getDeclaredMethod(methodName);
                        Type type = method.getReturnType();
                        Object subject = String.valueOf(method.invoke(obj));

                        if (!isConditionMet(String.valueOf(subject), predicate, condition, type)) {
                            return stopGo;
                        }
                    }else {
                        return stopGo;
                    }
                }


            }else{
                //if single lookup

                String subject = parts[0].trim();
                String predicate = parts[1].trim();

                if (httpResponse.data().containsKey(subject)) {
                    if (predicate.contains("'")) predicate = predicate.replace("'", "");
                    Object obj = httpResponse.get(subject);
                    if(obj != null){
                        String value = obj.toString();
                        checkCondition(line, stop, value, condition, predicate, entries);
                    }

                } else {
                    if (condition == "!=" &&
                            (predicate == "''" || predicate == "null")) {
                        return stopGo;
                    }
                }
            }
        }else{
            boolean notTrueExists = false;
            String subjectPre = parts[0].trim();
            if (subjectPre.startsWith("!")) {
                notTrueExists = true;
                subjectPre = subjectPre.replace("!", "");
            }

            //todo : resolve
            if(subjectPre.contains(".")) {
                String[] keys = subjectPre.split("\\.");
                String subject = keys[0];
                int startField = subjectPre.indexOf(".");
                String field = subjectPre.substring(startField + 1);
                if (httpResponse.data().containsKey(subject)) {
                    Object obj = httpResponse.get(subject);
                    Field fieldObj = obj.getClass().getDeclaredField(field);
                    fieldObj.setAccessible(true);
                    Object valueObj = fieldObj.get(obj);

                    Boolean isTrue = Boolean.valueOf(String.valueOf(valueObj));
                    if (isTrue == true && notTrueExists == true) {
                        return stopGo;
                    }
                    if (isTrue == false && notTrueExists == false) {
                        clearUxPartial(line, stop, entries);
                    }
                } else {
                    if (!notTrueExists) {
                        return stopGo;
                    }
                }


            }else{
                if (httpResponse.data().containsKey(subjectPre)) {
                    Object obj = httpResponse.get(subjectPre);
                    Boolean isTrue = Boolean.valueOf(String.valueOf(obj));
                    if (isTrue == true && notTrueExists == true) {
                        return stopGo;
                    }
                    if (isTrue == false && notTrueExists == false) {
                        return stopGo;
                    }
                } else {
                    if (!notTrueExists) {
                        return stopGo;
                    }
                }
            }

        }

        return null;
    }

    void checkCondition(int a6, int stop, String value, String condition, String predicate, List<String> entries){
        if (value.equals(predicate) && condition.equals("!=")) {
            clearUxPartial(a6, stop, entries);
        }
        if (!value.equals(predicate) && condition.equals("==")) {
            clearUxPartial(a6, stop, entries);
        }
    }

    List<Integer> getIgnoreEntries(int a6, int stop) {
        List<Integer> ignore = new ArrayList<>();
        for (int a4 = a6; a4 < stop; a4++) {
            ignore.add(a4);
        }
        return ignore;
    }

    private void clearUxPartial(int a6, int stop, List<String> entries) {
        for (int a4 = a6; a4 < stop; a4++) {
            entries.set(a4, "");
        }
    }

    private Boolean isConditionMet(String subject, String predicate, String condition, Type type) throws EosException {
        if (type.getTypeName().equals("int") || type.getTypeName().equals("java.lang.Integer")) {
            if (condition.equals(">")) {
                if (Integer.valueOf(String.valueOf(subject)) > Integer.valueOf(predicate))
                    return true;
            }
            if (condition.equals("<")) {
                if (Integer.valueOf(String.valueOf(subject)) < Integer.valueOf(predicate))
                    return true;
            }
            if (condition.equals("==")) {
                if (Integer.valueOf(String.valueOf(subject)) == Integer.valueOf(predicate))
                    return true;
            }
            if (condition.equals("<=")) {
                if (Integer.valueOf(String.valueOf(subject)) <= Integer.valueOf(predicate))
                    return true;
            }
            if (condition.equals(">=")) {
                if (Integer.valueOf(String.valueOf(subject)) >= Integer.valueOf(predicate))
                    return true;
            }
        } else {
            throw new EosException("integers only covered right now.");
        }
        return false;
    }

    private int getEachConditionStop(int a6, List<String> entries){
        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
            if(entries.get(a5).contains("</eos:if>"))return a5;
        }
        return a6;
    }

    private int getConditionStop(int a6, List<String> entries) {
        int startCount = 1;
        int endCount = 0;
        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
            String entry = entries.get(a5);
            if(entry.contains("</eos:if>")){
                endCount++;
            }
            if(entry.contains("<eos:if spec=")){
                startCount++;
            }
            if(startCount == endCount && entry.contains("</eos:if>")){
                return a5;
            }
        }
        return a6;
    }

    private String getCondition(String expression){
        if(expression.contains(">"))return ">";
        if(expression.contains("<"))return "<";
        if(expression.contains("=="))return "==";
        if(expression.contains(">="))return ">=";
        if(expression.contains("<="))return "<=";
        if(expression.contains("!="))return "!=";
        return "";
    }

    private void evaluateEachEntry(String entry, StringBuilder output, Object obj, String activeKey) throws NoSuchFieldException, IllegalAccessException {
        if(entry.contains("<eos:each"))return;
        if(entry.contains("</eos:each>"))return;
        if(entry.contains("<eos:if spec"))return;

        if(entry.contains("${")) {

            int startExpression = entry.indexOf("${");
            int endExpression = entry.indexOf("}", startExpression);
            String expression = entry.substring(startExpression, endExpression + 1);

            String[] keys = entry.substring(startExpression + 2, endExpression).split("\\.");

            if(keys[0].equals(activeKey)) {
                int startField = expression.indexOf(".");
                int endField = expression.indexOf("}");

                String field = expression.substring(startField + 1, endField);
                Object valueObj = getValueRecursive(0, field, obj);
                String value = "";
                if (valueObj != null) value = String.valueOf(valueObj);

                entry = entry.replace(expression, value);

                int startRemainder = entry.indexOf("${");
                if (startRemainder != -1) {
                    evaluateEntryRemainder(startExpression, entry, obj, output);
                } else {
                    output.append(entry + this.NEWLINE);
                }
            }
        }else{
            output.append(entry + this.NEWLINE);
        }
        System.out.println("zqo" + output);
    }

    private void evaluateEntryRemainder(int startExpressionRight, String entry, Object obj, StringBuilder output) throws NoSuchFieldException, IllegalAccessException {
        int startExpression = entry.indexOf("${", startExpressionRight - 1);
        int endExpression = entry.indexOf("}", startExpression);

        String expression = entry.substring(startExpression, endExpression + 1);

        int startField = expression.indexOf(".");
        int endField = expression.indexOf("}");

        String field = expression.substring(startField + 1, endField);
        Object valueObj = getValueRecursive(0, field, obj);
        String value = "";
        if(valueObj != null) value = String.valueOf(valueObj);

        entry = entry.replace(expression, value);

        int startRemainder = entry.indexOf("${", (startExpressionRight - value.length()));
        if(startRemainder != -1){
            evaluateEntryRemainder(startExpression, entry, obj, output);
        }else{
            output.append(entry + this.NEWLINE);
        }
    }


    private Iterable getIterableDeep(int a6, String entry, Object obj, List<String> entries) throws EosException, NoSuchFieldException, IllegalAccessException {
        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("in=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 4);//4 eq i.n.=.".
        String iterableKey = entry.substring(startIterate + 6, endIterate -1 );//in="${ and }

        String iterableFudge = "${" + iterableKey + "}";

        int startField = iterableFudge.indexOf(".");
        int endField = iterableFudge.indexOf("}", startField);
        String field = iterableFudge.substring(startField + 1, endField);

        int startItem = entry.indexOf("item=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 7);//item="
        String activeField = entry.substring(startItem + 6, endItem);

        List<Object> objs = (ArrayList) getIterableValueRecursive(0, field, obj);

        int start = a6 +1;
        Iterable iterable = new Iterable();
        int stop = getStopDeep(a6, entries);
        iterable.setGo(start);
        iterable.setStop(stop);
        iterable.setPojos(objs);
        iterable.setField(activeField);
        List<String> iterableEntries = getIterableEntries(start, stop, entries);
        iterable.setEntries(iterableEntries);
        return iterable;
    }

    private List<String> getIterableEntries(int start, int stop, List<String> entries){
        List<String> iterableEntries = new ArrayList<>();
        for(int baz = start; baz < stop; baz++){
            String entry = entries.get(baz);
            iterableEntries.add(entry);
        }
        return iterableEntries;
    }

    private Iterable getIterable(int a6, String entry, HttpResponse httpResponse, List<String> entries) throws EosException, NoSuchFieldException, IllegalAccessException {

        List<Object> objs = new ArrayList<>();
        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("in=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 4);//4 eq i.n.=.".
        String iterableKey = entry.substring(startIterate + 6, endIterate -1 );//in="${ and }

        int startItem = entry.indexOf("item=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 7);//items="
        String activeField = entry.substring(startItem + 6, endItem);

        String expression = entry.substring(startIterate + 4, endIterate + 1);

        if(iterableKey.contains(".")){
            objs = getIterableInitial(iterableKey, expression, httpResponse);
        }else if(httpResponse.data().containsKey(iterableKey)){
            objs = (ArrayList) httpResponse.get(iterableKey);
        }


        Iterable iterable = new Iterable();
        int go = a6 + 1;
        int stop = getStop(go, entries);

        List<String> iterableEntries = new ArrayList<>();
        for(int foo = 0; foo < stop; foo++){
            String iterableEntry = entries.get(foo);
            iterableEntries.add(iterableEntry);
        }

        iterable.setGo(go);
        iterable.setStop(stop);
        iterable.setPojos(objs);
        iterable.setField(activeField);
        iterable.setEntries(iterableEntries);
        return iterable;
    }

    private List<Object> getIterableInitial(String iterable, String expression, HttpResponse httpResponse) throws NoSuchFieldException, IllegalAccessException {
        int startField = expression.indexOf("${");
        int endField = expression.indexOf(".", startField);
        String key = expression.substring(startField + 2, endField);
        if(httpResponse.data().containsKey(key)){
            Object obj = httpResponse.get(key);
            Object objList = getIterableRecursive(iterable, expression, obj);
            return (ArrayList) objList;
        }
        return new ArrayList<>();
    }

    private List<Object> getIterableRecursive(String iterable, String expression, Object objBase) throws NoSuchFieldException, IllegalAccessException {
        List<Object> objs = new ArrayList<>();
        int startField = expression.indexOf(".");
        int endField = expression.indexOf("}");

        String field = expression.substring(startField + 1, endField);
        Object obj = getValueRecursive(0, field, objBase);

        if(obj != null){
            return (ArrayList) obj;
        }


        return objs;
    }

    private Object getIterableValueRecursive(int idx, String baseField, Object baseObj) throws NoSuchFieldException, IllegalAccessException {
        String[] fields = baseField.split("\\.");
        if(fields.length > 1){
            idx++;
            String key = fields[0];
            Field fieldObj = baseObj.getClass().getDeclaredField(key);
            if(fieldObj != null){
                fieldObj.setAccessible(true);
                Object obj = fieldObj.get(baseObj);
                int start = baseField.indexOf(".");
                String fieldPre = baseField.substring(start + 1);
                if(obj != null) {
                    return getValueRecursive(idx, fieldPre, obj);
                }
            }
        }else{
            Field fieldObj = baseObj.getClass().getDeclaredField(baseField);
            if(fieldObj != null) {
                fieldObj.setAccessible(true);
                Object obj = fieldObj.get(baseObj);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return new ArrayList();
    }

    private Object getValueRecursive(int idx, String baseField, Object baseObj) throws NoSuchFieldException, IllegalAccessException {
        String[] fields = baseField.split("\\.");
        if(fields.length > 1){
            idx++;
            String key = fields[0];
            Field fieldObj = baseObj.getClass().getDeclaredField(key);
            fieldObj.setAccessible(true);
            Object obj = fieldObj.get(baseObj);
            int start = baseField.indexOf(".");
            String fieldPre = baseField.substring(start + 1);
            if(obj != null) {
                return getValueRecursive(idx, fieldPre, obj);
            }

        }else{
            try {
                Field fieldObj = baseObj.getClass().getDeclaredField(baseField);
                fieldObj.setAccessible(true);
                Object obj = fieldObj.get(baseObj);
                if (obj != null) {
                    return obj;
                }
            }catch(Exception ex){}
        }
        return null;
    }

    private String evaluateEntry(int idx, int start, String activeField, String entry, HttpResponse httpResponse) throws NoSuchFieldException, IllegalAccessException, EosException, NoSuchMethodException, InvocationTargetException {

        if(entry.contains("${") &&
                !entry.contains("<eos:each") &&
                    !entry.contains("<eos:if")) {

            int startExpression = entry.indexOf("${", start);
            if(startExpression == -1)return entry;

            int endExpression = entry.indexOf("}", startExpression);
            String expression = entry.substring(startExpression, endExpression + 1);
            String fieldBase = entry.substring(startExpression + 2, endExpression);

            if(!fieldBase.equals(activeField)) {

                if (fieldBase.contains(".")) {
                    String[] fields = fieldBase.split("\\.");
                    String key = fields[0];

                    if (httpResponse.data().containsKey(key)) {
                        Object obj = httpResponse.get(key);

                        int startField = fieldBase.indexOf(".");
                        String passiton = fieldBase.substring(startField + 1);

                        //todo: allow for parameters?
                        if(passiton.contains("()")){
                            String method = passiton.replace("(", "")
                                    .replace(")", "");
                            try {
                                Method methodObj = obj.getClass().getDeclaredMethod(method);
                                Object valueObj = methodObj.invoke(obj);
                                String value = String.valueOf(valueObj);
                                entry = entry.replace(expression, value);
                            }catch(Exception ex){}
                        }else {
                            Object value = getValueRecursive(0, passiton, obj);
                            if (value != null) {
                                entry = entry.replace(expression, String.valueOf(value));
                            } else if (activeField.equals("")) {
                                //make empty!
                                entry = entry.replace(expression, "");
                            }
                        }

                    }else if(activeField.equals("")){
                        //make empty!
                        entry = entry.replace(expression, "");
                    }
                } else {
                    if (httpResponse.data().containsKey(fieldBase)) {
                        Object obj = httpResponse.get(fieldBase);
                        entry = entry.replace(expression, String.valueOf(obj));
                    }else if(activeField.equals("")){
                        entry = entry.replace(expression, "");
                    }
                }

                if (entry.contains("${")) {
                    idx++;
                    if(idx >= entry.length())return entry;
                    entry = evaluateEntry(idx,startExpression + idx, activeField, entry, httpResponse);
                }

            }
        }

        return entry;

    }

    private String invokeMethod(String fieldBase, Object obj) throws EosException {
        int startMethod = fieldBase.indexOf(".");
        int endMethod = fieldBase.indexOf("(", startMethod);
        String name = fieldBase.substring(startMethod + 1, endMethod)
                .replace("(", "");

        int startSig = fieldBase.indexOf("(");
        int endSig = fieldBase.indexOf(")");
        String paramFix = fieldBase.substring(startSig + 1, endSig);
        String[] parameters = paramFix.split(",");


        if(parameters.length > 0) {
            try {

                Method method = getObjMethod(name, obj);
                List<Object> finalParams = getMethodParameters(method, parameters);

                if (method != null) {
                    return String.valueOf(method.invoke(obj, finalParams.toArray()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            try {
                Method method = obj.getClass().getDeclaredMethod(name);
                if (method != null) {
                    return String.valueOf(method.invoke(obj));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    private Method getObjMethod(String methodName, Object obj) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for(Method method : methods){
            String namePre = method.getName();
            if(namePre.equals(methodName))return method;
        }
        return null;
    }

    private List<Object> getMethodParameters(Method method, String[] parameters) throws EosException {
        if(method.getParameterTypes().length != parameters.length)throw new EosException("parameters on " + method + " don't match.");

        List<Object> finalParams = new ArrayList<>();
        for(int a6 = 0; a6 < parameters.length; a6++){
            String parameter = parameters[a6];
            Type type = method.getParameterTypes()[a6];
            Object obj = null;
            if(type.getTypeName().equals("int") || type.getTypeName().equals("java.lang.Integer")){
                obj = Integer.valueOf(parameter);
            }
            if(type.getTypeName().equals("double") || type.getTypeName().equals("java.lang.Double")){
                obj = Double.valueOf(parameter);
            }
            if(type.getTypeName().equals("java.math.BigDecimal")){
                obj = new BigDecimal(parameter);
            }
            if(type.getTypeName().equals("float") || type.getTypeName().equals("java.lang.Float")){
                obj = Float.valueOf(parameter);
            }
            finalParams.add(obj);
        }

        return finalParams;
    }

    private int getStopDeep(int idx, List<String> entries) {
        int a10 = idx;
        for(int a6 = idx; a6 < entries.size(); a6++){
            String entry = entries.get(a6);
            if(entry.contains("</eos:each>")){
                return a6;
            }
        }
        return idx;
    }


    private int getStop(int baz, List<String> entries){
        int count = 0;
        boolean startRendered = false;
        for(int foo = baz; foo < entries.size(); foo++) {
            String entry = entries.get(foo);
            if(entry.contains("<eos:each")){
                startRendered = true;
            }

            if(!startRendered && entry.contains("</eos:each>")){
                return foo;
            }

            if(startRendered && entry.contains("</eos:each>")){
                if(count == 1){
                    return foo;
                }
                count++;
            }

        }
        return 0;
    }


}
