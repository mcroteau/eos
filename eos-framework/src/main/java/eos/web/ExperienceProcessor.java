package eos.web;

import com.sun.net.httpserver.HttpExchange;
import eos.exception.EosException;
import eos.model.web.*;

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
    final String IFSPEC  = "<eos:if";
    final String ENDIF   = "</eos:if>";

    Integer idx = 0;
    Integer idxn = 0;
    List<SpecPartial> specPartials = new ArrayList<>();
    Map<String, BasePartial> indexedPartials = new TreeMap<>();
    Map<String, BasePartial> sortedPartials = new TreeMap<>();
    List<BasePartial> partialsUnix = new ArrayList<>();


    public String execute(Map<String, Fragment> fragments, String view, HttpResponse resp, HttpRequest req, HttpExchange exchange) throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        List<String> stringEntries = Arrays.asList(view.split("\n"));
        List<BasePartial> basePartials = new ArrayList<>();

        for(String entry : stringEntries){
            BasePartial basePartial = getBasePartial(entry);
            basePartials.add(basePartial);
            System.out.println(">" + basePartial.getIdx());
        }

        List<BasePartial> partialsFoo = new ArrayList<>();
        Boolean iterableDiscovered = false;

        for(int foo = 0; foo < basePartials.size(); foo++) {
            BasePartial basePartial = basePartials.get(foo);
            String entry = basePartial.getEntry();

//            if (entry.contains(this.DATA)) {
//                setVariable(entry, resp);
//            } else if (entry.contains(this.IFSPEC) && !iterableDiscovered) {
//                SpecPartial specPartial = new SpecPartial();
//                specPartial.setEntry(entry);
//                specPartial.setSpec(entry);
//                setPartial(specPartial);
//                List<BasePartial> specPartials = getSpecPartials(foo, false, resp, basePartials);
//                specPartial.setPartials(specPartials);
//                partialsUnix.add(specPartial);
//            } else if (entry.contains(this.FOREACH)) {
//                IterablePartial iterablePartial = new IterablePartial();
//                MojosResult mojosResult = getIterableMojos(foo, entry, resp, basePartials);
//                iterablePartial.setMojosResult(mojosResult);
//                setPartial(iterablePartial);
//                List<BasePartial> iterablePartials = getIterablePartials(foo, false, resp, basePartials);
//                iterablePartial.setEntry(entry);
//                iterablePartial.setIdx(getIdxn());
//                iterablePartial.setPartials(iterablePartials);
//                partialsUnix.add(iterablePartial);
//                iterableDiscovered = true;
//            }else if(!iterableDiscovered){
//                BasicPartial basicPartial = new BasicPartial();
//                basicPartial.setEntry(entry);//heart broken.
//                basicPartial.setIdx(getIdxn());
//                setPartial(basePartial);
//                partialsUnix.add(basicPartial);
//            }
        }

        List<BasePartial> sighPartials = getBasePartials(0, basePartials.size(), false, resp, basePartials);

//        for(Map.Entry<String, BasePartial> mapEntry : indexedPartials.entrySet()){
//            BasePartial basePartial = mapEntry.getValue();
//            outputSpecs(basePartial);
//        }

        for(BasePartial basePartial : sighPartials){
            outputSpecs(basePartial);
        }

        return "";
    }

    void outputSpecs(BasePartial basePartial){
        if(basePartial.getType().equals(BasePartial.BASiC)){
            System.out.println("tni:" + basePartial.getEntry());
        }
        if(basePartial.getType().equals(BasePartial.SPeC)){
            SpecPartial specPartial = (SpecPartial) basePartial;
            List<BasePartial> specPartials = specPartial.getPartials();
            System.out.println("specs:" + specPartials.size());
            for(int foo = 0; foo < specPartials.size(); foo++){
                BasePartial basePartialDeux = specPartials.get(foo);
                outputSpecs(basePartialDeux);
            }
        }
        if(basePartial.getType().equals(BasePartial.ITeRABLE)){
            IterablePartial iterablePartial = (IterablePartial) basePartial;
            List<BasePartial> iterablePartials = iterablePartial.getPartials();
            System.out.println("iterables:" + iterablePartials.size());
            for(int foo = 0; foo < iterablePartials.size(); foo++){
                BasePartial basePartialDeux = iterablePartials.get(foo);
                outputSpecs(basePartialDeux);
            }
        }
    }

    Integer getEndSpec(int foo, List<BasePartial> basePartials) throws EosException {
        Integer openSpec = 1;
        Integer endSpec = 0;
        for (int qxro = foo + 1; qxro < basePartials.size(); qxro++) {
            BasePartial basePartial = basePartials.get(qxro);
            String basicEntry = basePartial.getEntry();
            if(basicEntry.contains(this.IFSPEC))openSpec++;
            if(basicEntry.contains(this.ENDIF))endSpec++;

            if(endSpec == openSpec){
                return qxro;
            }
        }
        return 0;
    }

    Integer getEndEach(int foo, Boolean secondPass, List<BasePartial> basePartials) throws EosException {
        Integer openEach = secondPass ? 1 : 0;
        Integer endEach = 0;
        /**
         1 each{
         2  each{
         3    foo
         4  }
         6 }
         */
        System.out.println("open:" + foo + " openEach:" + openEach);

        for (int qxro = foo; qxro < basePartials.size(); qxro++) {
            BasePartial basePartial = basePartials.get(qxro);
            String basicEntry = basePartial.getEntry();
            if(basicEntry.contains(this.ENDEACH))endEach++;

            System.out.println(">" + openEach + ":" + endEach);

            if(openEach > 3)throw new EosException("too many nested <eos:each>.");
            if(basicEntry.contains(this.ENDEACH) && endEach == openEach){
                System.out.println(">break : " + openEach + "==" + endEach);
                return qxro;
            }
        }
        throw new EosException("missing end </eos:each>");
    }

    List<BasePartial> getIterablePartials(int openIdx, Boolean secondPass, HttpResponse resp, List<BasePartial> basePartials) throws IllegalAccessException, EosException, NoSuchFieldException {
        Integer endEach = getEndEach(openIdx, secondPass, basePartials);
        List<BasePartial> iterablePartials = getBasePartials(openIdx, endEach, secondPass, resp, basePartials);
        return iterablePartials;
    }

    List<BasePartial> getBasePartials(int openIdx, int endIdx, Boolean secondPass, HttpResponse resp, List<BasePartial> basePartials) throws IllegalAccessException, EosException, NoSuchFieldException {

        List<BasePartial> xversedPartials = new ArrayList<>();

        for(int tqxro = openIdx; tqxro < endIdx; tqxro++){
            BasePartial basePartial = basePartials.get(tqxro);
            String basicEntry = basePartial.getEntry();

            if(basicEntry.contains(this.IFSPEC)){
                SpecPartial specPartial = new SpecPartial();
                specPartial.setIdx(getIdxn());
                specPartial.setSpec(basicEntry);
                List<BasePartial> specPartialsDeep = getSpecPartials(tqxro, false, resp, basePartials);
                specPartial.setPartials(specPartialsDeep);
                xversedPartials.add(specPartial);
                setPartial(specPartial);
                partialsUnix.add(specPartial);
            }else if(basicEntry.contains(this.FOREACH)){
                MojosResult mojosResultUne = getIterableMojos(tqxro, basicEntry, resp, basePartials);
                List<BasePartial> iterablePartialsUne = getIterablePartials(tqxro, secondPass, resp, basePartials);
                IterablePartial iterablePartialUne = new IterablePartial();
                iterablePartialUne.setIdx(getIdxn());
                iterablePartialUne.setEntry(basicEntry);
                iterablePartialUne.setMojosResult(mojosResultUne);
                iterablePartialUne.setPartials(iterablePartialsUne);
                xversedPartials.add(iterablePartialUne);
                partialsUnix.add(iterablePartialUne);
                System.out.println("une:"+ iterablePartialsUne.size());

                for(int blap = 0; blap < mojosResultUne.getMojos().size(); blap++) {
                    Object pojo = mojosResultUne.getMojos().get(blap);
                    for (int baz = 0; baz < iterablePartialsUne.size(); baz++) {

                        BasePartial iterablePartialFoo = iterablePartialsUne.get(baz);
                        String iterableEntryDeep = iterablePartialFoo.getEntry();

                        if(iterableEntryDeep.contains(this.IFSPEC)){
                            SpecPartial specPartial = new SpecPartial();
                            specPartial.setIdx(getIdxn());
                            specPartial.setSpec(iterableEntryDeep);
                            specPartial.setMojo(pojo);
                            List<BasePartial> specPartialsDeep = getSpecPartials(baz, secondPass, resp, iterablePartialsUne);
                            specPartial.setPartials(specPartialsDeep);
                            xversedPartials.add(specPartial);
                            partialsUnix.add(specPartial);
                            setPartial(specPartial);
                        }else if(iterableEntryDeep.contains(this.FOREACH)){

                            MojosResult mojosResultDos = getIterableMojosDeep(tqxro, iterableEntryDeep, pojo, basePartials);
                            List<BasePartial> iterablePartialsDos = getIterablePartials(baz, true, resp, iterablePartialsUne);
                            System.out.println("dp:"+ iterablePartialsDos.size());

                            IterablePartial iterablePartialDos = new IterablePartial();
                            iterablePartialDos.setIdx(getIdxn());
                            iterablePartialDos.setEntry(iterableEntryDeep);
                            iterablePartialDos.setMojosResult(mojosResultDos);
                            iterablePartialDos.setPartials(iterablePartialsDos);
                            xversedPartials.add(iterablePartialDos);
                            partialsUnix.add(iterablePartialDos);

                            for(int tike = 0; tike < mojosResultDos.getMojos().size(); tike++) {
                                Object mojo = mojosResultDos.getMojos().get(tike);
                                for (int abba = 0; abba < iterablePartialsDos.size(); abba++) {
                                    BasePartial iterablePartialTres = iterablePartialsDos.get(abba);
                                    String iterableEntryTres = iterablePartialTres.getEntry();
                                    if(iterableEntryTres.contains(this.IFSPEC)){
                                        SpecPartial specPartial = new SpecPartial();
                                        specPartial.setIdx(getIdxn());
                                        specPartial.setSpec(iterableEntryTres);
                                        specPartial.setMojo(mojo);
                                        List<BasePartial> specPartialsDeep = getSpecPartials(baz, false, resp, iterablePartialsUne);
                                        specPartial.setPartials(specPartialsDeep);
                                        partialsUnix.add(specPartial);
                                        xversedPartials.add(specPartial);
                                        setPartial(specPartial); // you guys are great! african americans i hurt.
                                    }else if(!iterableEntryTres.contains(this.FOREACH)){
                                        BasePartial partial = new BasicPartial();
                                        partial.setIdx(getIdxn());
                                        partial.setEntry(iterableEntryTres);
                                        partial.setMojo(pojo);
                                        partialsUnix.add(partial);
                                        xversedPartials.add(partial);
                                        setPartial(partial);
                                    }
                                }
                            }

                        }else{
                            BasePartial partial = new BasicPartial();
                            partial.setIdx(getIdxn());
                            partial.setEntry(iterableEntryDeep);
                            partial.setMojo(pojo);
                            partialsUnix.add(partial);
                            xversedPartials.add(partial);
                            setPartial(partial);
                        }
                    }
                }
            }else{
                BasePartial partial = new BasicPartial();
                partial.setIdx(getIdxn());
                partial.setEntry(basicEntry);
                partialsUnix.add(partial);
                xversedPartials.add(partial);
                setPartial(partial);
            }

        }

        return xversedPartials;
    }


    void setPartial(BasePartial basePartial){
        if(!indexedPartials.containsKey(getKey(basePartial)))
            indexedPartials.put(getKey(basePartial), basePartial);
    }

    String getKey(BasePartial basePartial){
        return basePartial.getIdx() + ":" + basePartial.getEntry();
    }


    Integer getIdxn() {
        idxn++; return idxn;
    }

    BasePartial getBasePartial(String entry) {
        return getBasePartial(entry, null, null);
    }

    BasePartial getBasePartial(String entry, Object mojo, Iterable iterable) {
        BasePartial basePartial = new BasicPartial();
        basePartial.setEntry(entry);
        basePartial.setMojo(mojo);
        basePartial.setIterable(iterable);
        basePartial.setIdx(idx); idx++;
        return basePartial;
    }


    boolean renderEntry(String entry){
        if(entry.contains(this.FOREACH))return false;
        if(entry.contains(this.ENDEACH))return false;
//        if(entry.contains(this.IFSPEC))return false;
//        if(entry.contains(this.ENDIF))return false;
        return true;
    }

    boolean withinIterable(BasicEntry basicEntry, List<BasicEntry> basicEntries){
        boolean inside = false;
        for(BasicEntry it : basicEntries){
            System.out.println(":::"  + it.getIdx() + "<" + basicEntry.getIdx() + ":" + it.getEntry());
            if(it.getEntry().contains(this.FOREACH))inside=true;
            if(inside && it.getIdx() < basicEntry.getIdx())return true;
        }
        return false;
    }

//    void exercisePartials(HttpResponse resp, List<BasePartial> partialsBaz) throws InvocationTargetException, NoSuchMethodException, EosException, IllegalAccessException, NoSuchFieldException {
//
//        List<BasePartial> partialsFoo = new ArrayList<>();
//
//        Boolean initialIteration = true;
//        Boolean innerIterableDiscovered = false;
//        Map<String, Boolean> specs = new HashMap<>();
//        for(int foo = 0; foo < partialsBaz.size(); foo++){
//            BasePartial basePartial = partialsBaz.get(foo);
//
//            IterablePartial iterablePartialFoo = null;
//
//            if(basePartial.getType().equals(BasePartial.ITeRABLE)) {
//                iterablePartialFoo = (IterablePartial) basePartial;
//            }
//
//            if(!basePartial.getType().equals(BasePartial.SPeC) && !basePartial.getType().equals(BasePartial.ITeRABLE)){
//                partialsFoo.add(basePartial);
//            }
//
//            if(iterablePartialFoo != null) {
//
//                List<BasePartial> iterableEntries = iterablePartialFoo.getIterable().getEntries();
//
//                for (int baz = 0; baz < iterableEntries.size(); baz++) {
//                    BasePartial iterablePartial = iterableEntries.get(baz);
//                    String iterableEntry = iterablePartial.getEntry();
//
//                    if (iterableEntry.contains(this.FOREACH)) {
//
//                        innerIterableDiscovered = true; initialIteration = false;
//                        Iterable deepIterable = getIterableDeep(baz, iterableEntry, iterablePartial.getMojo(), iterableEntries);
//
//                        for (int blurp = 0; blurp < deepIterable.getEntries().size(); blurp++) {
//                            BasePartial deepBasePartial = deepIterable.getEntries().get(blurp);
//                            deepBasePartial.setIdx(getIdxn());
//                            partialsFoo.add(deepBasePartial);
//                        }
//
//                    } else if (iterableEntry.contains(this.IFSPEC)) {
//                        SpecPartial specPartial = new SpecPartial();
//                        specPartial.setEntry(iterableEntry);
//                        specPartial.setIdx(getIdxn());
////                            specPartials.add(specPartial);//todo:remove spec partial
////                            entriesFoo.add(specBasicEntry);
//                        partialsFoo.add(specPartial);
//                    }else{
//                        BasicPartial basicPartial = new BasicPartial();
//                        basicPartial.setEntry(iterableEntry);
//                        basicPartial.setIdx(getIdxn());
//                        partialsFoo.add(basicPartial);
//                        specs.put(iterablePartial.getGuid(), true);
//                    }
//                }
//                initialIteration = true;
//            }
//        }
//
//        Integer number = 0;
//        List<BasePartial> entriesBaz = new ArrayList<>();
//        for(BasePartial partial : partialsFoo){
//            String entry = partial.getEntry();
//            if(renderEntry(entry)) {
//                System.out.println("spec: " + partial.getIdx() + " > " + entry);
//            }
//        }
//
//    }

//    Integer getNombre(BasicEntry basicEntry, List<BasicEntry> entriesFoo) {
//        int mojosCount = basicEntry.getIterable().getMojosCount();
//        int count = 0;
//        for(int foo = basicEntry.getNumber(); foo < entriesFoo.size(); foo++){
//            BasicEntry beekon = entriesFoo.get(foo);
//            if(beekon.getEntry().contains(this.ENDIF))count++;
//            if(mojosCount == count)return beekon.getNumber();
//        }
//        return null;
//    }


    boolean withinDeepIterable(Integer idx, List<BasePartial> partials) {
        Integer count = 0;
        for(BasePartial partial : partials){
            BasicEntry basicEntry = partial.getBasicEntry();
            String entry = basicEntry.getEntry();
            if(entry.contains(this.FOREACH))count++;
            boolean n = (count >= 2);
            boolean z = (idx >= basicEntry.getIdx());
            System.out.println("count >= 2 && idx >= basicEntry.getIdx() " + n + " : " + z + " count:" + count + " : " + idx + " : " + basicEntry.getIdx());
            if(count >= 2 && idx >= basicEntry.getIdx()){
                return true;
            }
        }
        return false;
    }

    boolean withinIterableDeep(int blurp, List<BasicEntry> entries) {
        for(int bank = 0; bank < entries.size(); bank++){
            BasicEntry basicEntry = entries.get(bank);
            String entry = basicEntry.getEntry();
            if(entry.contains(this.FOREACH) && blurp > basicEntry.getNumber()){
                return true;
            }
        }
        return false;
    }


    //todo: condense it for hurt
    //why are you sad
    public int getSpecStop(List<BasePartial> basePartials){
        int startCount = 1, endCount = 0;
        for(int baz = 1; baz < basePartials.size(); baz++){
            BasePartial basePartial = basePartials.get(baz);
            String entry = basePartial.getEntry();
            if(entry.contains(this.ENDIF)){
                endCount++;
            }
            if(entry.contains(this.IFSPEC)){
                startCount++;
            }
            if(startCount == endCount &&
                    entry.contains(this.ENDIF)){
                return baz;
            }
        }
        System.out.println("***********>>>>>> returning line");
        return 0;
    }


//    public Boolean insideIterable(int lineNumber, List<IterablePartial> iterablePartials){
//        for(int waldo = 0; waldo < iterablePartials.size(); waldo++){
//            IterablePartial iterablePartial = iterablePartials.get(waldo);
//            if(lineNumber > iterablePartial.getIterable().getGo() &&
//                    lineNumber < iterablePartial.getIterable().getStop()){
//                return true;
//            }
//        }
//        return false;
//    }


    private boolean renderIterableSpec(String entry, String activeField, Object obj, Iterable iterable, HttpResponse resp) throws NoSuchFieldException, IllegalAccessException {

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

            if (field.equals(activeField)) {

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
                    if(subject.trim().equals(""))subject = "'" + subject + "'";

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


    List<BasePartial> getSpecPartials(int openIdx, Boolean secondPass, HttpResponse resp, List<BasePartial> basePartials) throws IllegalAccessException, EosException, NoSuchFieldException {
        Integer endEach = getEndSpec(openIdx, basePartials);
        List<BasePartial> specPartials = getBasePartials(openIdx, endEach, secondPass, resp, basePartials);
        return specPartials;
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

    private int getIterableSpecStop(int idx, List<BasicEntry> entries){
        for(int foo = idx; foo < entries.size(); foo++){
            if(entries.get(foo).getEntry().contains(this.ENDIF))return entries.get(foo).getIdx();
        }
        return idx;
    }

//    private int getEvaluateStop(int a6, List<String> entries) {
//        int startCount = 1;
//        int endCount = 0;
//        for(int a5 = a6 + 1; a5 < entries.size(); a5++){
//            String entry = entries.get(a5);
//            if(entry.contains(this.ENDIF)){
//                endCount++;
//            }
//            if(entry.contains(this.IFSPEC)){
//                startCount++;
//            }
//            if(startCount == endCount && entry.contains("</eos:if>")){
//                return a5;
//            }
//        }
//        return a6;
//    }

    private String getCondition(String expression){
        if(expression.contains(">"))return ">";
        if(expression.contains("<"))return "<";
        if(expression.contains("=="))return "==";
        if(expression.contains(">="))return ">=";
        if(expression.contains("<="))return "<=";
        if(expression.contains("!="))return "!=";
        return "";
    }

    private String evaluateEachEntry(String activeField, String entry, Iterable iterable, Object mojo) throws NoSuchFieldException, IllegalAccessException {

        if(entry.contains(this.FOREACH))return "";
        if(entry.contains(this.ENDEACH))return "";
        if(entry.contains(this.IFSPEC))return "";
        if(entry.contains(this.ENDIF))return "";

        if(entry.contains("${")) {

            int startExpression = entry.indexOf("${");
            int endExpression = entry.indexOf("}", startExpression);
            String expression = entry.substring(startExpression, endExpression + 1);

            String[] keys = entry.substring(startExpression + 2, endExpression).split("\\.");

            if(keys[0].equals(activeField)) {

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

    private MojosResult getIterableMojosDeep(int go, String entry, Object mojo, List<BasePartial> basePartials) throws EosException, NoSuchFieldException, IllegalAccessException {
        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("items=", startEach + 1);
        int endIterate = entry.indexOf("\"", startIterate + 7);//items="
        String iterableKey = entry.substring(startIterate + 9, endIterate -1 );//items="${ and }

        String iterablePadded = "${" + iterableKey + "}";

        int startField = iterablePadded.indexOf(".");
        int endField = iterablePadded.indexOf("}", startField);
        String field = iterablePadded.substring(startField + 1, endField);

        int startItem = entry.indexOf("var=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 5);//var="
        String activeField = entry.substring(startItem + 5, endItem);

        List<Object> mojos = (ArrayList) getIterableValueRecursive(0, field, mojo);
//
//        Iterable iterable = new Iterable();
//        int stop = getStop(go, basePartials);
//        List<BasePartial> iterablePartials = new ArrayList<>();
//        for(int foo = go + 1; foo < stop; foo++){
//            BasePartial iterablePartial = basePartials.get(foo);
//            String iterableEntry = iterablePartial.getEntry();
//            if(iterableEntry.contains(this.IFSPEC)){
//                SpecPartial specPartial = new SpecPartial();
//                specPartial.setEntry(iterableEntry);
//                List<BasePartial> specPartials = getSpecEntries(basePartials);
//                specPartial.setPartials(specPartials);
//                iterablePartials.add(specPartial);
//            }else {
//                BasicPartial basicPartial = new BasicPartial();
//                basicPartial.setEntry(iterableEntry);
//                iterablePartials.add(basicPartial);
//            }
//        }
//
//        List<BasePartial> partialsFoo = new ArrayList<>();
//        for(Object koko : mojos){
//            for(BasePartial basePartial : iterablePartials){
//                basePartial.setMojo(koko);
//                basePartial.setIterable(iterable);
//                partialsFoo.add(basePartial);
//            }
//        }
//
//
//        BasicEntry basicEntry = new BasicEntry();
//        basicEntry.setEntry(entry);
//
//        iterable.setBasicEntry(basicEntry);
//        iterable.setGo(go + 1);//todo:
//        iterable.setStop(stop);//todo:
//        iterable.setMojosCount(mojos.size());
////        iterable.setMojos(mojos);//todo:? do i need
//        iterable.setField(activeField);
//        iterable.setEntries(partialsFoo);//todo: change name to setPartials()
//        return iterable;

        MojosResult mojosResult = new MojosResult();
        mojosResult.setField(activeField);
        mojosResult.setMojos(mojos);
        return mojosResult;
    }

    private List<BasicEntry> getIterableEntries(int go, int stop, List<BasicEntry> entries){
        List<BasicEntry> iterableEntries = new ArrayList<>();
        for(int baz = go + 1; baz < stop; baz++){
            BasicEntry entry = entries.get(baz);
            iterableEntries.add(entry);
        }
        return iterableEntries;
    }

    private MojosResult getIterableMojos(int go, String entry, HttpResponse httpResponse, List<BasePartial> basePartials) throws EosException, NoSuchFieldException, IllegalAccessException {

        int startEach = entry.indexOf("<eos:each");

        int startIterate = entry.indexOf("items=", startEach);
        int endIterate = entry.indexOf("\"", startIterate + 7);//items=".
        String iterableKey = entry.substring(startIterate + 9, endIterate -1 );//items="${ }

        int startItem = entry.indexOf("var=", endIterate);
        int endItem = entry.indexOf("\"", startItem + 6);//items="
        String activeField = entry.substring(startItem + 5, endItem);

        String expression = entry.substring(startIterate + 7, endIterate);

        List<Object> mojos = new ArrayList<>();
        if(iterableKey.contains(".")){
            mojos = getIterableInitial(expression, httpResponse);
        }else if(httpResponse.data().containsKey(iterableKey)){
            mojos = (ArrayList) httpResponse.get(iterableKey);
        }

//        Iterable iterable = new Iterable();
//        int stop = getStop(go, basePartials);
//        List<BasePartial> iterablePartials = new ArrayList<>();
//        for(int foo = go + 1; foo < stop; foo++){
//            BasePartial basicIterableEntry = basePartials.get(foo);
//            String iterableEntry = basicIterableEntry.getEntry();
//            if(iterableEntry.contains(this.IFSPEC)){
//                SpecPartial specPartial = new SpecPartial();
//                specPartial.setEntry(iterableEntry);
//                specPartial.setIdx(getIdxn());
//                List<BasePartial> specPartials = getSpecEntries(basePartials);
//                specPartial.setPartials(specPartials);
//                iterablePartials.add(specPartial);
//            } else {
//                BasicPartial basicPartial = new BasicPartial();
//                basicPartial.setEntry(iterableEntry);
//                basicPartial.setIdx(getIdxn());
//                iterablePartials.add(basicPartial);
//            }
//        }
//
//        List<BasePartial> partialsFoo = new ArrayList<>();
//        for(Object koko : mojos){
//            for(BasePartial basePartial : iterablePartials){
//                basePartial.setMojo(koko);
//                basePartial.setIterable(iterable);
//                partialsFoo.add(basePartial);
//            }
//        }
//
//        BasicEntry basicEntry = new BasicEntry();
//        basicEntry.setEntry(entry);
//
//        iterable.setBasicEntry(basicEntry);
//        iterable.setGo(go + 1);//todo:
//        iterable.setStop(stop);//todo:
//        iterable.setMojosCount(mojos.size());
////        iterable.setMojos(mojos);//todo:? do i need
//        iterable.setField(activeField);
//        iterable.setEntries(partialsFoo);
//        return iterable;
        MojosResult mojosResult = new MojosResult();
        mojosResult.setField(activeField);
        mojosResult.setMojos(mojos);
        return mojosResult;
    }

    private List<Object> getIterableInitial(String expression, HttpResponse httpResponse) throws NoSuchFieldException, IllegalAccessException {
        int startField = expression.indexOf("${");
        int endField = expression.indexOf(".", startField);
        String key = expression.substring(startField + 2, endField);
        if(httpResponse.data().containsKey(key)){
            Object obj = httpResponse.get(key);
            Object objList = getIterableRecursive(expression, obj);
            return (ArrayList) objList;
        }
        return new ArrayList<>();
    }

    private List<Object> getIterableRecursive(String expression, Object objBase) throws NoSuchFieldException, IllegalAccessException {
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


    private int getStop(int bap, List<BasePartial> basePartials){
        int count = 0;
        boolean startRendered = false;
        for(int foo = bap + 1; foo < basePartials.size(); foo++) {
            BasePartial partial = basePartials.get(foo);
            String entry = partial.getEntry();
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
