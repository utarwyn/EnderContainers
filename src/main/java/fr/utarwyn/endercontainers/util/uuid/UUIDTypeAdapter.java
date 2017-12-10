package fr.utarwyn.endercontainers.util.uuid;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * Class used with the Gson library to adapt JSON
 * @since 2.0.0
 * @author Utarwyn
 */
public class UUIDTypeAdapter extends TypeAdapter<UUID> {

	public void write(JsonWriter out, UUID value) throws IOException {
		out.value(fromUUID(value));
	}

	public UUID read(JsonReader in) throws IOException {
		return fromString(in.nextString());
	}

	/**
	 * Format an UUID into string without hyphens
	 * @param value UUID to format
	 * @return Formatted UUID string
	 */
	public static String fromUUID(UUID value) {
		return value.toString().replace("-", "");
	}

	/**
	 * Format and convert a string into an UUID
	 * @param input String to convert
	 * @return UUID generated from the string parameter
	 */
	private static UUID fromString(String input) {
		return UUID.fromString(input.replaceFirst(
				"(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

}