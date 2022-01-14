package a8i.web;

import a8i.model.web.HttpRequest;
import com.sun.net.httpserver.HttpExchange;

/**
 * Is the interface to implement when
 * write your own custom user interface
 * Pointcuts
 *
 * A Pointcut is a tag implementation to
 * either perform a piece of logic or
 * check if a condition is true
 */
public interface Pointcut {

    /**
     * key:attribute tells the ui processor
     * to look for <key:attribute> tags
     * within your view file
     */
    final String KEY         = "key:attribute";

    /**
     * EVALUATION : true means isTrue() will be invoked
     * false means halloween will be invoked
     */
    final Boolean EVALUATION = false;

    /**
     *
     * @return returns KEY, to be used by A8i
     */
    public String getKey();

    /**
     * Is used by A8i to determine whether or not
     * to invoke halloween() or isTrue()
     *
     * @return returns EVALUATION field
     */
    public Boolean isEvaluation();


    /**
     *
     * @param httpRequest
     * @param exchange
     * @return boolean if the condition supplied is true or false
     */
    public boolean isTrue(HttpRequest httpRequest, HttpExchange exchange);

    /**
     *
     * @param httpRequest
     * @param exchange
     * @return returns a string to be inserted into the view where the
     *  pointcut key tag is placed. <key:attribute/>
     */
    public String halloween(HttpRequest httpRequest, HttpExchange exchange);

}
