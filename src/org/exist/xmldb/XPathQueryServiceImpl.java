package org.exist.xmldb;

import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

/**
 * Extends {@link org.xmldb.api.modules.XPathQueryService} by additional
 * methods specific to eXist.
 * 
 * @author Wolfgang <wolfgang@exist-db.org>
 *
 */
public interface XPathQueryServiceImpl extends XPathQueryService {

	/**
	 * Process an XPath query based on the result of a previous query.
	 * The XMLResource contains the result received from a previous
	 * query.
	 * 
	 * @param res an XMLResource as obtained from a previous query.
	 * @param query the XPath query
	 */
    public ResourceSet query( XMLResource res, String query )
    throws XMLDBException;
    
    /**
     * Process an XPath query based on the result of a previous query and sort the
     * results using the second XPath expression. The XMLResource contains 
     * the result received from a previous query.
	 * 
     * @param res an XMLResource as obtained from a previous query
     * @param query the XPath query
     * @param sortExpr another XPath expression, which is executed relative to
     * the results of the primary expression. The result of applying sortExpr is converted
     * to a string value, which is then used to sort the results. 
     * @throws XMLDBException
     */
    public ResourceSet query( XMLResource res, String query, String sortExpr)
    throws XMLDBException;
    
    /**
     * Process an XPath query and sort the results by applying a second XPath expression
     * to each of the search results. The result of applying the sort expression is converted
     * into a string, which is then used to sort the set of results.
     * 
     * @param query the XPath query
     * @param sortExpr another XPath expression, which is executed relative to the
     * results of the primary expression.
     * @return
     * @throws XMLDBException
     */
    public ResourceSet query( String query, String sortExpr)
    throws XMLDBException;
    
    /**
     * Declare an external XPath variable and assign a value to it.
     * 
     * A variable can be referenced inside an XPath expression as
     * <b>$variable</b>. For example, if you declare a variable with
     * 
     * <pre>
     * 	declareVariable("name", "HAMLET");
     * </pre>
     * 
     * you may use the variable in an XPath expression as follows:
     * 
     * <pre>
     * 	//SPEECH[SPEAKER=$name]
     * </pre>
     * 
     * Any Java object may be passed as initial value. The query engine will try
     * to map this into a corresponding XPath value. You may also pass an 
     * XMLResource as obtained from another XPath expression. This will be
     * converted into a node.
     *  
     * @param qname a valid QName by which the variable is identified. Any
     * prefix should have been mapped to a namespace, i.e. if a variable is called
     * <b>x:name</b>, there should be a prefix/namespace mapping for the prefix
     * x
     * @param initialValue the initial value, which is assigned to the variable
     * 
     * @throws XMLDBException
     */
    public void declareVariable(String qname, Object initialValue) throws XMLDBException;
    
    /**
	 * Execute all following queries in a protected environment.
	 * Protected means: it is guaranteed that documents referenced by the
	 * query or the result set are not modified by other threads
	 * until {@link #endProtected} is called.
	 */
    public void beginProtected();
    
    /**
	 * Close the protected environment. All locks held
	 * by the current thread are released. The result set
	 * is no longer guaranteed to be stable.
	 */
	public void endProtected();
}
