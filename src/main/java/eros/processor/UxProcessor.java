package eros.processor;

import eros.exception.A8iException;
import eros.model.Iterable;
import eros.model.web.HttpRequest;
import eros.model.web.HttpResponse;
import eros.web.Pointcut;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class UxProcessor {

    final String NEWLINE = "\r\n";
    final String FOREACH = "<a:each";

    public String process(Map<String, Pointcut> pointcuts, String view, HttpResponse httpResponse, HttpRequest request, HttpExchange exchange) throws A8iException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        List<String> entries = Arrays.asList(view.split("\n"));
        evaluatePointcuts(request, exchange, entries, pointcuts);

        for(int a6 = 0; a6 < entries.size(); a6++) {
            String entryBase = entries.get(a6);
            if(entryBase.contains("<a:set")){
                setVariable(entryBase, httpResponse);
            }

            if(entryBase.contains(this.FOREACH)) {
                Iterable iterable = getIterable(a6, entryBase, httpResponse, entries);
                StringBuilder eachOut = new StringBuilder();
//                System.out.println("z" + iterable.getStop() + ":" +  entries.get(iterable.getStop()));

                for(int a7 = 0; a7 < iterable.getPojos().size(); a7++) {
                    Object obj = iterable.getPojos().get(a7);
                    List<Integer> ignore = new ArrayList<>();

                    for (int a8 = iterable.getStart(); a8 < iterable.getStop(); a8++) {
                        String entry = entries.get(a8);
                        if(entry.contains("<a:if condition=")){
                            ignore = evaluateEachCondition(a8, entry, obj, httpResponse, entries);
                        }
                        if(ignore.contains(a8))continue;
                        if(entry.contains(this.FOREACH))continue;
                        entry = evaluateEntry(0, 0, iterable.getField(), entry, httpResponse);
                        if(entry.contains("<a:set")){
                            setEachVariable(entry, httpResponse, obj);
                        }
                        evaluateEachEntry(entry, eachOut, obj, iterable.getField());
                    }
                }

                entries.set(a6, eachOut.toString());
                entries.set(iterable.getStop(), "");
                for(int a7 = a6 + 1; a7 < iterable.getStop(); a7++){
                    entries.set(a7, "");
                }
            }else {

                if(entryBase.contains("<a:if condition=")){
                    evaluateCondition(a6, entryBase, httpResponse, entries);
                }

                entryBase = evaluateEntry(0, 0, "", entryBase, httpResponse);
                entries.set(a6, entryBase);
            }
        }

        StringBuilder output = new StringBuilder();
        for(int a6 = 0; a6 < entries.size(); a6++) {
            output.append(entries.get(a6) + this.NEWLINE);
        }

        StringBuilder finalOut = retrieveFinal(output);
        return finalOut.toString();
    }

    private List<Integer> evaluateEachCondition(int a8, String entry, Object obj, HttpResponse httpResponse, List<String> entries) throws NoSuchFieldException, IllegalAccessException {
        List<Integer> ignore = new ArrayList<>();

        int stop = getEachConditionStop(a8, entries);
        int startIf = entry.indexOf("<a:if condition=");

        int startExpression = entry.indexOf("${", startIf);
        int endExpression = entry.indexOf("}", startExpression);

        String expressionNite = entry.substring(startExpression, endExpression +1);
        String expression = entry.substring(startExpression + 2, endExpression);

        String condition = getCondition(expression);
        String[] bits = expression.split(condition);

        String subjectPre = bits[0].trim();
        String predicatePre = bits[1].trim();


        //<a:if condition="${town.id == organization.townId}">

        //todo:?2 levels
        //todo: switch
        if(subjectPre.contains(".")) {

            int startSubject = subjectPre.indexOf(".");
            String subjectKey = subjectPre.substring(startSubject + 1).trim();

            Object subjectObj = getValueRecursive(0, subjectKey, obj);
            String subject = String.valueOf(subjectObj);

            if(predicatePre.equals("null")){

                if(subjectObj == null && condition.equals("!=")){
                    ignore = getIgnoreEntries(a8, stop);
                }
                if(subjectObj != null && condition.equals("==")){
                    ignore = getIgnoreEntries(a8, stop);
                }

            }else {
                String[] predicateKeys = predicatePre.split("\\.");
                String key = predicateKeys[0];
                String field = predicateKeys[1];

                Object keyObj = httpResponse.get(key);
                Field fieldObj = keyObj.getClass().getDeclaredField(field);
                fieldObj.setAccessible(true);
                String predicate = String.valueOf(fieldObj.get(keyObj));

                if (predicate.equals(subject) && condition.equals("!=")) {
                    ignore = getIgnoreEntries(a8, stop);
                }
                if (!predicate.equals(subject) && condition.equals("==")) {
                    ignore = getIgnoreEntries(a8, stop);
                }
            }

        }else{
            //todo: one key
        }
        String a = entries.get(a8);
        String b = entries.get(stop);
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

    private void evaluatePointcuts(HttpRequest request, HttpExchange exchange, List<String> entries, Map<String, Pointcut> pointcuts) {

        for(Map.Entry<String, Pointcut> entry: pointcuts.entrySet()){
            Pointcut pointcut = entry.getValue();
            String key = pointcut.getKey();//dice:rollem in <dice:rollem> is key

            String open = "<" + key + ">";
            String rabbleDos = "<" + key + "/>";
            String close = "</" + key + ">";
            for(int a6 = 0; a6 < entries.size(); a6++) {
                String entryBase = entries.get(a6);

                if(entryBase.trim().startsWith("<!--"))entries.set(a6, "");
                if(entryBase.trim().startsWith("<%--"))entries.set(a6, "");

                if(entryBase.contains(rabbleDos) &&
                        !pointcut.isEvaluation()){
                    String output = pointcut.halloween(request, exchange);
                    if(output != null) {
                        entryBase = entryBase.replace(rabbleDos, output);
                        entries.set(a6, entryBase);
                    }
                }

                if(entryBase.contains(open)){
                    int stop = getAttributeClose(a6, close, entries);
                    if(pointcut.isEvaluation()){
                        Boolean isTrue = pointcut.isTrue(request, exchange);
                        if(!isTrue){
                            for(int a4 = a6; a4 < stop; a4++){
                                entries.set(a4, "");
                            }
                        }
                    }
                    if(!pointcut.isEvaluation()){
                        String output = pointcut.halloween(request, exchange);
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

    private void evaluateCondition(int a6, String entry, HttpResponse httpResponse, List<String> entries) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, A8iException, NoSuchFieldException {
        int stop = getConditionStop(a6, entries);
        int startIf = entry.indexOf("<a:if condition=");

        int startExpression = entry.indexOf("${", startIf);
        int endExpression = entry.indexOf("}", startExpression);

        String expressionNite = entry.substring(startExpression, endExpression +1);
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

                if(!keys[1].contains("(")){
                    String field = keys[1].trim();

                    if (httpResponse.data().containsKey(objName)) {

                        if(predicate != null && predicate.equals("")) {

                            Object obj = httpResponse.get(objName);
                            Field fieldObj = obj.getClass().getDeclaredField(field);

                            fieldObj.setAccessible(true);
                            Object valueObj = fieldObj.get(obj);
                            Type type = fieldObj.getType();
                            String value = String.valueOf(valueObj);

                            //todo: test the following
                            switch (type.getTypeName()) {
                                case "java.lang.String":

                                    if (value.equals(predicate) && condition.equals("!=")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    if (!value.equals(predicate) && condition.equals("==")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    break;

                                case "java.lang.Integer":

                                    Integer valueInt = Integer.valueOf(value);
                                    Integer predicateInt = Integer.valueOf(predicate);
                                    if (valueInt.equals(predicateInt) && condition.equals("!=")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    if (!valueInt.equals(predicateInt) && condition.equals("==")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    break;

                                case "java.lang.Boolean":

                                    Boolean valueBool = Boolean.valueOf(value);
                                    Boolean predicateBool = Boolean.valueOf(predicate);
                                    if (valueBool.equals(predicateBool) && condition.equals("!=")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    if (!valueBool.equals(predicateBool) && condition.equals("==")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    break;


                                case "java.math.BigDecimal":

                                    BigDecimal valueBd = new BigDecimal(value);
                                    BigDecimal predicateBd = new BigDecimal(predicate);
                                    if (valueBd.equals(predicateBd) && condition.equals("!=")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    if (!valueBd.equals(predicateBd) && condition.equals("==")) {
                                        clearUxPartial(a6, stop, entries);
                                    }
                                    break;
                                default:
                                    break;

                            }

                        }

                    }else{
                        clearUxPartial(a6, stop, entries);
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
                            clearUxPartial(a6, stop, entries);
                        }
                    }else {
                        clearUxPartial(a6, stop, entries);
                    }
                }


            }else{
                //if single lookup

                String subject = parts[0].trim();
                String predicate = parts[1].trim();

                if(httpResponse.data().containsKey(subject)){

                    if(predicate.contains("''"))predicate = predicate.replaceAll("'","");

                    String value = String.valueOf(httpResponse.get(subject));
                    if(value.equals(predicate) && condition.equals("!=")){
                        clearUxPartial(a6, stop, entries);
                    }
                    if(!value.equals(predicate) && condition.equals("==")){
                        clearUxPartial(a6, stop, entries);
                    }

                }else{
                    if(condition.equals("!=") &&
                            (predicate.equals("''") || predicate.equals("null"))){
                        clearUxPartial(a6, stop, entries);
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
                        clearUxPartial(a6, stop, entries);
                    }
                    if (isTrue == false && notTrueExists == false) {
                        clearUxPartial(a6, stop, entries);
                    }
                } else {
                    if (!notTrueExists) {
                        clearUxPartial(a6, stop, entries);
                    }
                }


            }else{
                if (httpResponse.data().containsKey(subjectPre)) {
                    Object obj = httpResponse.get(subjectPre);
                    Boolean isTrue = Boolean.valueOf(String.valueOf(obj));
                    if (isTrue == true && notTrueExists == true) {
                        clearUxPartial(a6, stop, entries);
                    }
                    if (isTrue == false && notTrueExists == false) {
                        clearUxPartial(a6, stop, entries);
                    }
                } else {
                    if (!notTrueExists) {
                        clearUxPartial(a6, stop, entries);
                    }
                }
            }

        }

        entries.set(a6, "");
        entries.set(stop, "");
        entry.replace(expressionNite, "condition issue : '" + expression + "'");

    }

    private List<Integer> getIgnoreEntries(int a6, int stop) {
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

    private Boolean isConditionMet(String subject, String predicate, String condition, Type type) throws A8iException {
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
            throw new A8iException("integers only covered right now.");
        }
        return false;
    }

    private int getEachConditionStop(int a6, List<String> entries){
        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
            if(entries.get(a5).contains("</a:if>"))return a5;
        }
        return a6;
    }

    private int getConditionStop(int a6, List<String> entries) {
        int startCount = 1;
        int endCount = 0;
        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
            String entry = entries.get(a5);
            if(entry.contains("</a:if>")){
                endCount++;
            }
            if(entry.contains("<a:if condition=")){
                startCount++;
            }
            if(startCount == endCount && entry.contains("</a:if>")){
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

    private void iterateEvaluate(Iterable iterable, HttpResponse httpResponse, List<String> entries) throws NoSuchFieldException, IllegalAccessException, A8iException, NoSuchMethodException, InvocationTargetException {
        for(int a6 = iterable.getStart(); a6 < iterable.getStop(); a6++){
            String entryBase = entries.get(a6);
            String entry = evaluateEntry(0, 0, iterable.getField(), entryBase, httpResponse);
            entries.set(a6, entry);
        }
    }


    private StringBuilder retrieveFinal(StringBuilder eachOut){
        StringBuilder finalOut = new StringBuilder();
        String[] parts = eachOut.toString().split("\n");
        for(String bit : parts){
            if(!bit.isEmpty())finalOut.append(bit + this.NEWLINE);
        }
        return finalOut;
    }


    private void retrofit(int a6, int size, List<String> entries){
        for(int a10 = a6; a10 < a6 + size + 1; a10++){
            entries.set(a10, "");
        }
    }

    private void resolveIterable(StringBuilder eachOut, Iterable iterable, HttpResponse httpResponse, List<String> entries) throws NoSuchFieldException, IllegalAccessException, A8iException, NoSuchMethodException, InvocationTargetException {
        for(int a7 = 0; a7 < iterable.getPojos().size(); a7++){
            Object obj = iterable.getPojos().get(a7);
            for(int a8 = iterable.getStart(); a8 < iterable.getStop(); a8++){
                String entry = entries.get(a8);
                iterateEvaluate(iterable, httpResponse, entries);
                evaluateEachEntry(entry, eachOut, obj, iterable.getField());
            }
        }
    }

    private void evaluateEachEntry(String entry, StringBuilder output, Object obj, String activeKey) throws NoSuchFieldException, IllegalAccessException {
        if(entry.contains("<a:each"))return;
        if(entry.contains("</a:each>"))return;
        if(entry.contains("<a:if condition"))return;

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


    private Iterable getIterableObj(int a6, String entry, Object obj, List<String> entries) throws A8iException, NoSuchFieldException, IllegalAccessException {
        List<Object> objs;
        int startEach = entry.indexOf("<a:each");

        int startIterate = entry.indexOf("in=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 4);//4 eq i.n.=.".
        String iterableKey = entry.substring(startIterate + 6, endIterate -1 );//in="${ and }

        String iterableFudge = "${" + iterableKey + "}";

        int startField = iterableFudge.indexOf(".");
        int endField = iterableFudge.indexOf("}", startField);
        String field = iterableFudge.substring(startField + 1, endField);

        int startItem = entry.indexOf("item=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 8);
        String activeField = entry.substring(startItem + 6, endItem);

        objs = (ArrayList) getIterableValueRecursive(0, field, obj);

        Iterable iterable = new Iterable();
        int stop = getStopDeep(a6, entries);
        iterable.setStart(a6 + 1);
        iterable.setStop(stop);
        iterable.setPojos(objs);
        iterable.setField(activeField);
        return iterable;
    }

    private Iterable getIterable(int a6, String entry, HttpResponse httpResponse, List<String> entries) throws A8iException, NoSuchFieldException, IllegalAccessException {
        List<Object> objs = new ArrayList<>();
        int startEach = entry.indexOf("<a:each");

        int startIterate = entry.indexOf("in=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 4);//4 eq i.n.=.".
        String iterableKey = entry.substring(startIterate + 6, endIterate -1 );//in="${ and }

        int startItem = entry.indexOf("item=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 8);
        String activeField = entry.substring(startItem + 6, endItem);

        String expression = entry.substring(startIterate + 4, endIterate + 1);

        if(iterableKey.contains(".")){
            objs = getIterableInitial(iterableKey, expression, httpResponse);
        }else if(httpResponse.data().containsKey(iterableKey)){
            objs = (ArrayList) httpResponse.get(iterableKey);
        }


        Iterable iterable = new Iterable();
        int stop = getStop(a6 + 1, entries);
        iterable.setStart(a6 + 1);
        iterable.setStop(stop);
        iterable.setPojos(objs);
        iterable.setField(activeField);
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

    private String evaluateEntry(int idx, int start, String activeField, String entry, HttpResponse httpResponse) throws NoSuchFieldException, IllegalAccessException, A8iException, NoSuchMethodException, InvocationTargetException {

        if(entry.contains("${") &&
                !entry.contains("<a:each") &&
                    !entry.contains("<a:if")) {

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

    private String invokeMethod(String fieldBase, Object obj) throws A8iException {
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

    private List<Object> getMethodParameters(Method method, String[] parameters) throws A8iException {
        if(method.getParameterTypes().length != parameters.length)throw new A8iException("parameters on " + method + " don't match.");

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
            if(entry.contains("</a:each>")){
                return a6;
            }
        }
        return idx;
    }


    private int getStop(int a6, List<String> entries){
        int count = 0;
        boolean startRendered = false;
        for(int a4 = a6; a4 < entries.size(); a4++) {
            String entry = entries.get(a4);
            if(entry.contains("<a:each")){
                startRendered = true;
            }

            if(!startRendered && entry.contains("</a:each>")){
                return a4;
            }

            if(startRendered && entry.contains("</a:each>")){
                if(count == 1){
                    return a4;
                }
                count++;
            }

        }
        return 0;
    }


}
