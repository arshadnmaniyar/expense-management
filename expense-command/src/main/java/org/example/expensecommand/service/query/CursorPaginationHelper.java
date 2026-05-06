package org.example.expensecommand.service.query;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * Handles cursor-based pagination encoding/decoding.
 * Cursor is base64-encoded offset used for pagination.
 */
@Slf4j
public class CursorPaginationHelper {

    /**
     * Decodes cursor to offset integer.
     * If cursor is invalid or null, returns 0.
     */
    public static int decodeOffset(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return 0;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Integer.parseInt(decoded);
        } catch (Exception e) {
            log.warn("Invalid cursor provided, defaulting to offset 0: {}", cursor);
            return 0;
        }
    }

    /**
     * Encodes offset to base64 cursor string.
     * Returns null if there are no more results.
     */
    public static String encodeOffset(int offset, int resultSize, int limit) {
        if (resultSize <= limit) {
            // No more results
            return null;
        }

        try {
            String offsetStr = String.valueOf(offset + limit);
            return Base64.getEncoder().encodeToString(offsetStr.getBytes());
        } catch (Exception e) {
            log.error("Failed to encode cursor for offset: {}", offset, e);
            return null;
        }
    }
}
