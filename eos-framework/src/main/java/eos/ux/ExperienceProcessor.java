package eos.ux;

import com.sun.net.httpserver.HttpExchange;
import eos.annotate.Basic;
import eos.exception.EosException;
import eos.model.web.*;
import eos.model.web.Iterable;
import eos.web.Fragment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class ExperienceProcessor {

    final String NEWLINE = "\n";
    final String DATA    = "<eos:set var=";
    final String FOREACH = "<eos:each";
    final String ENDEACH = "</eos:each>";
    final String IFSPEC  = "<eos:if spec=";
    final String ENDIF   = "</eos:if>";

    List<BasePartial> partials;
    List<StopGo> stopGos;

    public ExperienceProcessor(){
        partials = new ArrayList<>();
        stopGos = new ArrayList<>();
    }

    public String process(Map<String, Fragment> fragments, String view, HttpResponse resp, HttpRequest req, HttpExchange exchange) throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        List<String> stringEntries = Arrays.asList(view.split("\n"));
        List<BasicEntry> basicEntries = new ArrayList<>();
        for(String entry : stringEntries){
            BasicEntry basicEntry = new BasicEntry();
            basicEntry.setEntry(entry);
            basicEntries.add(basicEntry);
        }

        List<IterablePartial> iterablePartials = new ArrayList<>();
//        List<SpecPartial> specPartials = new ArrayList<>();
        List<BasicPartial> basicPartials = new ArrayList<>();

        for(int thud = 0; thud < basicEntries.size(); thud++){
            BasicEntry basicThudEntry = basicEntries.get(thud);
            String entry = basicThudEntry.getEntry();

            BasicEntry basicEntry = new BasicEntry();
            basicEntry.setEntry(entry);

            if(!entry.trim().equals("")) {
                if (entry.contains(this.DATA)) {
                    setVariable(entry, resp);
                } else if (entry.contains(this.FOREACH) &&
                        !withinIterable(thud, iterablePartials)) {
                    IterablePartial iterablePartial = new IterablePartial();
                    Iterable iterable = getIterable(thud, entry, resp, basicEntries);
                    iterablePartial.setGo(thud);
                    iterablePartial.setEntry(basicEntry);
                    iterablePartial.setEntries(iterable.getEntries());
                    iterablePartial.setIterable(iterable);
                    iterablePartials.add(iterablePartial);
                } else if (entry.contains(this.IFSPEC) && !withinIterable(thud, iterablePartials)) {
                    SpecPartial specPartial = new SpecPartial();
                    specPartial.setGo(thud);
                    specPartial.setEntry(basicEntry);
//                    StopGo stopGo = getSpecStopGo(thud, entries);
//                    specPartial.setStopGo(stopGo);
//                    List<String> specEntries = getSpecEntries(stopGo, entries);
//                    specPartial.setEntries(specEntries);
//                    specPartials.add(specPartial);
                    partials.add(specPartial);
                } else {
                    BasicPartial basicPartial = new BasicPartial();
                    //Q loves you
                    //you found out.
                    basicPartial.setGo(thud);
                    basicPartial.setEntry(basicEntry);
                    basicPartials.add(basicPartial);
                    partials.add(basicPartial);
                }
            }
        }

//        List<StopGo> specStopGos = evaluateSpecs(resp, specPartials);
//        stopGos.addAll(specStopGos);

        /**
         * who ever is saying stop is connected to the richmonds, they have the birds eye view
         */

        evaluateForEach(resp, iterablePartials);

        System.out.println("....\n\n");
        for(BasePartial partial : partials){
            BasicEntry basicEntry = partial.getEntry();
            String entry = basicEntry.getEntry();
            if(entry != null &&
                    exercisePartial(entry)){
                if(partial.getType().equals(BasePartial.SPeC) &&
                        (renderSpec(entry, resp) ||
                            renderIterableSpec(entry, basicEntry.getMojo(), basicEntry.getIterable(), resp))) {
                    System.out.println(entry);
                }else{
                    System.out.println(entry);
                }
            }
        }

        return "";
    }

    boolean exercisePartial(String entry){
        if(entry.contains(this.FOREACH))return false;
        if(entry.contains(this.ENDEACH))return false;
        if(entry.contains(this.IFSPEC))return false;
        if(entry.contains(this.ENDIF))return false;
        return true;
    }

    boolean withinIterable(int thud, List<IterablePartial> iterablePartials){
        for(int foo = 0; foo < iterablePartials.size(); foo++){
            IterablePartial iterablePartial = iterablePartials.get(foo);
            if(thud > iterablePartial.getIterable().getGo() &&
                    thud < iterablePartial.getIterable().getStop()){
                return true;
            }
        }
        return false;
    }

//    List<StopGo> evaluateSpecs(HttpResponse resp, List<SpecPartial> specPartials) throws NoSuchMethodException, EosException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
//        List<StopGo> specStopGos = new ArrayList<>();
//        for(int foo = 0; foo < specPartials.size(); foo++){
//            SpecPartial specPartial = specPartials.get(foo);
//            List<String> specEntries = specPartial.getEntries();
//            String specEntry = specEntries.get(0);
//            StopGo stopGo = evaluateSpec(0, specEntry, resp, specEntries);
//            specStopGos.add(stopGo);
//        }
//
//        return specStopGos;
//    }

//    public List<StopGo> evaluateSpecsDeep(Iterable iterable, HttpResponse resp) throws NoSuchMethodException, EosException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
//        List<StopGo> specStopGos = new ArrayList<>();
//        for(int foo = 0; foo < specPartials.size(); foo++){
//            SpecPartial specPartial = specPartials.get(foo);
//            List<BasicEntry> specEntries = specPartial.getEntries();
//            BasicEntry basicSpecEntry = specEntries.get(0);
//            String specEntry = basicSpecEntry.getEntry();
//            Object mojo = basicSpecEntry.getMojo();
//            StopGo stopGo = evaluateSpecDeep(0, specEntry, mojo, iterable, resp, specEntries);
//            specStopGos.add(stopGo);
//        }
//        return specStopGos;
//    }


    int iterables = 0;

    void evaluateForEach(HttpResponse resp, List<IterablePartial> iterablePartials) throws InvocationTargetException, NoSuchMethodException, EosException, IllegalAccessException, NoSuchFieldException {

        System.out.println("size< iterables * " + iterables + iterablePartials.size());

        for(int foo = 0; foo < iterablePartials.size(); foo++){
            iterables++;
            IterablePartial iterablePartial = iterablePartials.get(foo);
            System.out.println("iterablePartial " + iterablePartial.getIterable().getEntry().getEntry());
            Iterable iterable = iterablePartial.getIterable();
            System.out.println("& " + iterablePartial.getIterable().getEntries().size());
            for(int baz = 0; baz < iterablePartial.getIterable().getEntries().size(); baz++){
                System.out.println("+" + iterablePartial.getIterable().getEntries().get(baz));
            }

            List<SpecPartial> deepSpecPartials = new ArrayList<>();
            List<BasicPartial> deepBasicPartials = new ArrayList<>();
            List<Object> mojos = iterablePartial.getIterable().getMojos();

            for(int bar = 0; bar < mojos.size(); bar++) {

                Object mojo = mojos.get(bar);
                List<BasicEntry> iterableEntries = iterablePartial.getEntries();

                for (int baz = 0; baz < iterableEntries.size(); baz++) {
                    BasicEntry basicEntry = iterableEntries.get(baz);
                    String entry = basicEntry.getEntry();

                    if(entry.contains("${todo.id}")){
                        System.out.println("aksjdaokjds aksdj ");
                    }

                    if(!entry.trim().equals("")) {

                        BasicEntry basicMojoEntry = new BasicEntry();
                        basicMojoEntry.setEntry(entry);
                        basicMojoEntry.setMojo(mojo);
                        basicMojoEntry.setIterable(iterable);

                        if (entry.contains(this.DATA)) {
                            setEachVariable(entry, resp, mojo);
                        } else if (entry.contains(this.FOREACH)) {
                            System.out.println("...." + entry);
                            IterablePartial deepIterablePartial = new IterablePartial();
                            deepIterablePartial.setGo(baz);
                            deepIterablePartial.setEntry(basicMojoEntry);
                            Iterable deepIterable = getIterableDeep(baz, entry, mojo, iterableEntries);
                            deepIterablePartial.setEntries(deepIterable.getEntries());
                            deepIterablePartial.setIterable(deepIterable);
                            partials.add(deepIterablePartial);

                            for(int bang = 0; bang < deepIterable.getMojos().size(); bang++){
                                Object bojo = deepIterable.getMojos().get(bang);
                                for(int blurp = 0; blurp < deepIterable.getMojos().size(); blurp++) {
                                    BasicEntry deepIterableEntry = deepIterable.getEntries().get(blurp);
                                    String deepEntry = deepIterableEntry.getEntry();
                                    System.out.println("!" + deepEntry);
                                    BasicEntry deepBasicEntry = new BasicEntry();
                                    deepBasicEntry.setMojo(bojo);
                                    deepBasicEntry.setEntry(deepEntry);
                                    BasicPartial basicPartial = new BasicPartial();
                                    basicPartial.setGo(baz);
                                    basicPartial.setEntry(deepBasicEntry);
//                                deepBasicPartials.add(basicPartial);
                                    partials.add(basicPartial);
                                }
                            }



                        } else if (entry.contains(this.IFSPEC)) {
                            SpecPartial specPartial = new SpecPartial();
                            StopGo stopGo = getSpecStopGo(baz, iterableEntries);
                            specPartial.setStopGo(stopGo);
                            List<BasicEntry> specEntries = getSpecEntries(stopGo, iterableEntries);
                            specPartial.setEntries(specEntries);
                            specPartial.setEntry(basicMojoEntry);
                            deepSpecPartials.add(specPartial);

//                            List<StopGo> deepSpecStopGos = evaluateSpecsDeep(mojo, iterable, resp, deepSpecPartials);
//                            deepSpecStopGos.add(stopGo);
//                            stopGos.addAll(deepSpecStopGos);
                            partials.add(specPartial);

                        } else {
                            System.out.println("*** " + entry);
                            BasicPartial basicPartial = new BasicPartial();
                            basicPartial.setGo(baz);
                            basicPartial.setEntry(basicMojoEntry);
//                            deepBasicPartials.add(basicPartial);
                            partials.add(basicPartial);
                        }

                    }
                }
            }
        }
    }

    int count = 0;
    public boolean exerciseIterablePartial(IterablePartial partial, List<StopGo> specStopGos){
        if(specStopGos.size() == 0) return true;
        for(int foo = 0; foo < specStopGos.size(); foo++){
            StopGo stopGo = specStopGos.get(foo);
            if(stopGo != null) {
                Iterable iterable = partial.getIterable();
                int stop = iterable.getStop();
                int go = iterable.getGo();
                if (go > stopGo.getGo() && stop < iterable.getStop()) {
                    return false;
                }
            }
        }
        count++;
        System.out.println("\n\n count " + count);
        return true;
    }


    public StopGo getSpecStopGo(int line, List<BasicEntry> entries){
        StopGo stopGo = new StopGo();
        int stop = getSpecStop(line, entries);
        stopGo.setStop(stop);
        stopGo.setGo(line);
        return stopGo;
    }

    public List<BasicEntry> getSpecEntries(StopGo stopGo, List<BasicEntry> entries){
        List<BasicEntry> specEntries = new ArrayList<>();
        for(int foo = stopGo.getGo(); foo < stopGo.getStop(); foo++){
            BasicEntry basicEntry = entries.get(foo);
            specEntries.add(basicEntry);
        }
        return specEntries;
    }

    public int getSpecStop(int line, List<BasicEntry> entries){
        int startCount = 1, endCount = 0;
        for(int baz = line + 1; baz < entries.size(); baz++){
            BasicEntry basicEntry = entries.get(baz);
            String entry = basicEntry.getEntry();
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


    private boolean renderIterableSpec(String entry, Object obj, Iterable iterable, HttpResponse resp) throws NoSuchFieldException, IllegalAccessException {

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

                if (predicatePre.equals("null")) {

                    if (subjectObj == null && condition.equals("==")) {
                        return true;
                    }
                    if (subjectObj != null && condition.equals("!=")) {
                        return true;
                    }

                } else if (predicatePre.contains(".")) {

                    String[] predicateKeys = predicatePre.split("\\.");
                    String key = predicateKeys[0];

                    int startField = predicatePre.indexOf(".");
                    String passiton = predicatePre.substring(startField + 1);

                    Object keyObj = resp.get(key);
                    Object value = getValueRecursive(0, passiton, keyObj);

                    String predicate = String.valueOf(value);

                    if (predicate.equals(subject) && condition.equals("==")) {
                        return true;
                    }
                    if (!predicate.equals(subject) && condition.equals("!=")) {
                        return true;
                    }

                } else if (!predicatePre.contains("'")) {
                    if (predicatePre.equals(subject) && condition.equals("==")) {
                        return true;
                    }
                    if (!predicatePre.equals(subject) && condition.equals("!=")) {
                        return true;
                    }
                } else if (predicatePre.contains("'")) {
                    if (predicatePre.equals("''")) {
                        if(subject.equals(""))subject = "'" + subject + "'";

                        if(!predicatePre.equals(subject) && condition.equals("!=")){
                            return true;
                        }
                        if(predicatePre.equals(subject) && condition.equals("==")){
                            return true;
                        }
                    } else {
                        String predicate = predicatePre.replaceAll("'", "");
                        if (predicate.equals(subject) && condition.equals("==")) {
                            return true;
                        }
                        if (!predicate.equals(subject) && condition.equals("!=")) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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

    private boolean renderSpec(String entry, HttpResponse httpResponse) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, EosException, NoSuchFieldException {

        int startIf = entry.indexOf("<eos:if spec=");

        int startExpression = entry.indexOf("${", startIf);
        int endExpression = entry.indexOf("}", startExpression);
        String expression = entry.substring(startExpression + 2, endExpression);

        String spec = getCondition(expression);
        String[] parts;
        if(!spec.equals("")) {
            parts = expression.split(spec);
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

                    if (httpResponse.data().containsKey(objName)) {
                        if(predicate.contains("'")) predicate = predicate.replace("'", "");
                        if (predicate != null && predicate != "") {
                            Object obj = httpResponse.get(objName);
                            String value = getValueRecursive(0, query, obj).toString();
                            if (value.equals(predicate) && spec.equals("==")) {
                                return true;
                            }
                            if (!value.equals(predicate) && spec.equals("!=")) {
                                return true;
                            }
                        }
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

                        if (isConditionMet(String.valueOf(subject), predicate, spec, type)) {
                            return true;
                        }
                    }
                }

            }else{
                String subject = parts[0].trim();
                String predicate = parts[1].trim();

                if (httpResponse.data().containsKey(subject)) {
                    if (predicate.contains("'")) predicate = predicate.replace("'", "");
                    Object obj = httpResponse.get(subject);
                    if(obj != null){
                        String value = obj.toString();
                        if (value.equals(predicate) && spec.equals("==")) {
                            return true;
                        }
                        if (!value.equals(predicate) && spec.equals("!=")) {
                            return true;
                        }
                    }

                } else {
                    if (spec == "==" &&
                            (predicate == "''" || predicate == "null")) {
                        return true;
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
                    if (isTrue == true && notTrueExists == false) {
                        return true;
                    }
                    if (isTrue == false && notTrueExists != true) {
                        return true;
                    }
                }

            }else{
                if (httpResponse.data().containsKey(subjectPre)) {
                    Object obj = httpResponse.get(subjectPre);
                    Boolean isTrue = Boolean.valueOf(String.valueOf(obj));

                    if (isTrue == true && notTrueExists != true) {
                        return true;
                    }
                    if (isTrue == false && notTrueExists == true) {
                        return true;
                    }
                }
            }

        }

        return false;
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

    private int getIterableSpecStop(int line, List<String> entries){
        for(int foo = line + 1; foo < entries.size(); foo++){
            if(entries.get(foo).contains(this.ENDIF))return foo;
        }
        return line;
    }

    private int getEvaluateStop(int a6, List<String> entries) {
        int startCount = 1;
        int endCount = 0;
        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
            String entry = entries.get(a5);
            if(entry.contains(this.ENDIF)){
                endCount++;
            }
            if(entry.contains(this.IFSPEC)){
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

    private String evaluateEachEntry(String entry, Iterable iterable, Object mojo) throws NoSuchFieldException, IllegalAccessException {

        if(entry.contains(this.FOREACH))return "";
        if(entry.contains(this.ENDEACH))return "";
        if(entry.contains(this.IFSPEC))return "";
        if(entry.contains(this.ENDIF))return "";

        if(entry.contains("${")) {

            int startExpression = entry.indexOf("${");
            int endExpression = entry.indexOf("}", startExpression);
            String expression = entry.substring(startExpression, endExpression + 1);

            String[] keys = entry.substring(startExpression + 2, endExpression).split("\\.");

            System.out.println("a asdadsasd " + keys[0].equals(iterable.getField()) + ":" + keys[0] + " > " + iterable.getField() + " ' " + entry);
            for(int t = 0; t < iterable.getEntries().size(); t++){
                System.out.println(iterable.getEntries().get(t));
            }

            if(keys[0].equals(iterable.getField())) {

                System.out.println("aksjdfaskjfd");
                int startField = expression.indexOf(".");
                int endField = expression.indexOf("}");

                String field = expression.substring(startField + 1, endField);
                Object valueObj = getValueRecursive(0, field, mojo);
                String value = "";
                if (valueObj != null) value = String.valueOf(valueObj);

                entry = entry.replace(expression, value);

                int startRemainder = entry.indexOf("${");
                if (startRemainder != -1) {
                    return evaluateEntryRemainder(startExpression, entry, mojo, new StringBuilder());
                } else {
                    return entry + this.NEWLINE;
                }
            }
        }else{
            return entry + this.NEWLINE;
        }
        return "";
    }

    private String evaluateEntryRemainder(int startExpressionRight, String entry, Object obj, StringBuilder output) throws NoSuchFieldException, IllegalAccessException {
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
        return output.toString();
    }


    private Iterable getIterableDeep(int go, String entry, Object obj, List<BasicEntry> entries) throws EosException, NoSuchFieldException, IllegalAccessException {
        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("items=", startEach + 1);
        int endIterate = entry.indexOf("\"", startIterate + 7);//items="
        String iterableKey = entry.substring(startIterate + 9, endIterate -1 );//items="${ and }

        String iterableFudge = "${" + iterableKey + "}";

        int startField = iterableFudge.indexOf(".");
        int endField = iterableFudge.indexOf("}", startField);
        String field = iterableFudge.substring(startField + 1, endField);

        int startItem = entry.indexOf("var=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 6);//var="
        String activeField = entry.substring(startItem + 5, endItem);

        System.out.println("*  " + iterableFudge + " * " + activeField + " * " + field);

        List<Object> objs = (ArrayList) getIterableValueRecursive(0, field, obj);

        Iterable iterable = new Iterable();
        int stop = getStop(go, entries);
        List<BasicEntry> iterableEntries = new ArrayList<>();
        for(int foo = go; foo < stop; foo++){
            BasicEntry basicIterableEntry = entries.get(foo);
            String iterableEntry = basicIterableEntry.getEntry();
            System.out.println(">>>>" + iterableEntry);
            BasicEntry basicEntry = new BasicEntry();
            basicEntry.setEntry(iterableEntry);
            iterableEntries.add(basicEntry);
        }

        BasicEntry basicEntry = new BasicEntry();
        basicEntry.setEntry(entry);

        iterable.setEntry(basicEntry);
        iterable.setGo(go);
        iterable.setStop(stop);
        iterable.setMojos(objs);
        iterable.setField(activeField);
        iterable.setEntries(iterableEntries);
        return iterable;
    }

    private List<String> getIterableEntries(int go, int stop, List<String> entries){
        List<String> iterableEntries = new ArrayList<>();
        for(int baz = go + 1; baz < stop; baz++){
            String entry = entries.get(baz);
            iterableEntries.add(entry);
        }
        return iterableEntries;
    }

    private Iterable getIterable(int go, String entry, HttpResponse httpResponse, List<BasicEntry> entries) throws EosException, NoSuchFieldException, IllegalAccessException {

        List<Object> objs = new ArrayList<>();
        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("items=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 7);//items=".
        String iterableKey = entry.substring(startIterate + 9, endIterate -1 );//items="${ }

        int startItem = entry.indexOf("var=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 6);//items="
        String activeField = entry.substring(startItem + 5, endItem);

        String expression = entry.substring(startIterate + 7, endIterate);

        if(iterableKey.contains(".")){
            objs = getIterableInitial(iterableKey, expression, httpResponse);
        }else if(httpResponse.data().containsKey(iterableKey)){
            objs = (ArrayList) httpResponse.get(iterableKey);
        }

        Iterable iterable = new Iterable();
        int stop = getStop(go, entries);

        List<BasicEntry> iterableEntries = new ArrayList<>();
        for(int foo = go + 1; foo < stop; foo++){
            BasicEntry iterableEntry = entries.get(foo);
            BasicEntry basicEntry = new BasicEntry();
            basicEntry.setEntry(iterableEntry.getEntry());
            iterableEntries.add(basicEntry);
        }

        BasicEntry basicEntry = new BasicEntry();
        basicEntry.setEntry(entry);

        iterable.setEntry(basicEntry);
        iterable.setGo(go);
        iterable.setStop(stop);
        iterable.setMojos(objs);
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
        for(int thud = 0; thud < parameters.length; thud++){
            String parameter = parameters[thud];
            Type type = method.getParameterTypes()[thud];
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
        for(int thud = idx; thud < entries.size(); thud++){
            String entry = entries.get(thud);
            if(entry.contains("</eos:each>")){
                return thud;
            }
        }
        return idx;
    }


    private int getStop(int thud, List<BasicEntry> entries){
        int count = 0;
        boolean startRendered = false;
        for(int foo = thud + 1; foo < entries.size(); foo++) {
            BasicEntry basicEntry = entries.get(foo);
            String entry = basicEntry.getEntry();
            if(entry.contains(this.FOREACH)){
                startRendered = true;
            }

            if(!startRendered && entry.contains(this.ENDEACH)){
                return foo + 1;
            }

            if(startRendered && entry.contains(this.ENDEACH)){
                if(count == 1){
                    return foo + 1;
                }
                count++;
            }

        }
        return 0;
    }

    private StringBuilder getOutput(StringBuilder eachOut){
        StringBuilder finalOut = new StringBuilder();
        String[] parts = eachOut.toString().split("\n");
        for(String bit : parts){
            if(!bit.trim().equals(""))finalOut.append(bit + this.NEWLINE);
        }
        return finalOut;
    }

}
