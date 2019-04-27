package fr.utarwyn.endercontainers.database.request;

/**
 * Request interface
 * @since 2.2.0
 * @author Utarwyn <maxime.malgorn@laposte.net>
 */
public interface IRequest {

	/**
	 * Generate the final request to be executed on the database
	 * @return generated request
	 */
	String getRequest();

	/**
	 * Return all attributes added to this request
	 * @return List of attributes objects
	 */
	Object[] getAttributes();

}
