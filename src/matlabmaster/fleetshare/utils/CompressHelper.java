// CompressHelper.java
package matlabmaster.fleetshare.utils; // Adjust package name as needed

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Helper class for compressing and decompressing strings using Deflate (Zlib),
 * with Base64 encoding for safe text transmission/storage.
 * This implementation avoids GZIPOutputStream/GZIPInputStream and uses
 * Deflater/Inflater directly, potentially working around script restrictions.
 */
public class CompressHelper {

    // Buffer size for compression/decompression chunks
    private static final int BUFFER_SIZE = 1024;

    /**
     * Compresses a string using the Deflate algorithm (RFC 1951, used within Zlib)
     * and encodes the result in Base64.
     *
     * @param data The string to compress. Must not be null.
     * @return A Base64-encoded string of the Deflate-compressed data.
     *         Returns an empty string if the input data is empty.
     * @throws IOException If an error occurs during the compression process.
     */
    public static String compress(String data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("Input data string cannot be null");
        }
        if (data.isEmpty()) {
            return "";
        }

        // Convert input string to bytes
        byte[] inputBytes = data.getBytes(StandardCharsets.UTF_8);

        // Create a Deflater instance. Using DEFAULT_COMPRESSION level.
        // Z_SYNC_FLUSH ensures all output is produced promptly.
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true); // true = No Zlib header
        deflater.setInput(inputBytes);
        deflater.finish(); // Indicate that we've provided all input

        // Use ByteArrayOutputStream-like logic with byte arrays
        // Pre-allocate with input size, will grow if needed
        byte[] outputBuffer = new byte[inputBytes.length];
        int totalCompressedBytes = 0;

        // Compress data in chunks
        while (!deflater.finished()) {
            // Ensure buffer has space
            if (totalCompressedBytes >= outputBuffer.length) {
                // Grow buffer if needed
                byte[] newBuffer = new byte[outputBuffer.length * 2];
                System.arraycopy(outputBuffer, 0, newBuffer, 0, outputBuffer.length);
                outputBuffer = newBuffer;
            }
            // Perform compression step
            int compressedBytes = deflater.deflate(outputBuffer, totalCompressedBytes, outputBuffer.length - totalCompressedBytes, Deflater.SYNC_FLUSH);
            totalCompressedBytes += compressedBytes;
        }
        deflater.end(); // Clean up

        // Trim the output buffer to the actual compressed size
        byte[] finalCompressedData = new byte[totalCompressedBytes];
        System.arraycopy(outputBuffer, 0, finalCompressedData, 0, totalCompressedBytes);

        // Encode compressed bytes to Base64 string for text safety
        return Base64.getEncoder().encodeToString(finalCompressedData);
    }

    /**
     * Decodes a Base64 string and then decompresses it using the Inflate algorithm
     * (RFC 1951, used within Zlib).
     *
     * @param compressedDataBase64 The Base64-encoded, Deflate-compressed string.
     *                             Must not be null.
     * @return The original uncompressed string.
     *         Returns an empty string if the input data is empty.
     * @throws IOException If an I/O error occurs during decompression,
     *                     or if the input is not valid Base64, or if decompression fails.
     */
    public static String decompress(String compressedDataBase64) throws IOException {
        if (compressedDataBase64 == null) {
            throw new IllegalArgumentException("Input compressed data string cannot be null");
        }
        if (compressedDataBase64.isEmpty()) {
            return "";
        }

        // Decode Base64 string to get compressed bytes
        byte[] compressedBytes;
        try {
            compressedBytes = Base64.getDecoder().decode(compressedDataBase64);
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to decode Base64 input. The input might be corrupted or not Base64 encoded.", e);
        }

        // Create an Inflater instance. true = No Zlib header (raw deflate)
        Inflater inflater = new Inflater(true);
        inflater.setInput(compressedBytes);

        // Use ByteArrayOutputStream-like logic with byte arrays
        // Pre-allocate with compressed size, will grow if needed
        byte[] outputBuffer = new byte[Math.max(compressedBytes.length * 2, BUFFER_SIZE)];
        int totalOutputBytes = 0;
        byte[] tempBuffer = new byte[BUFFER_SIZE]; // Temporary buffer for inflater.inflate()

        // Decompress data in chunks
        while (!inflater.finished() && !inflater.needsInput()) {
            try {
                int decompressedBytes = inflater.inflate(tempBuffer);

                // Ensure main output buffer has space
                while (totalOutputBytes + decompressedBytes > outputBuffer.length) {
                    byte[] newBuffer = new byte[outputBuffer.length * 2];
                    System.arraycopy(outputBuffer, 0, newBuffer, 0, outputBuffer.length);
                    outputBuffer = newBuffer;
                }

                // Copy decompressed chunk to main output buffer
                if (decompressedBytes > 0) {
                    System.arraycopy(tempBuffer, 0, outputBuffer, totalOutputBytes, decompressedBytes);
                    totalOutputBytes += decompressedBytes;
                } else if (decompressedBytes == 0 && !inflater.finished()) {
                    // If no bytes were produced but inflater is not finished,
                    // it might need more input or the data is corrupt.
                    // However, since we provided all input, it's likely finished or needsInput.
                    // Let the loop condition handle it.
                    break; // Prevent potential infinite loop
                }
            } catch (DataFormatException e) {
                throw new IOException("Failed to decompress data. The input might be corrupted or not valid Deflate data.", e);
            }
        }
        inflater.end(); // Clean up

        // Convert decompressed bytes back to string
        return new String(outputBuffer, 0, totalOutputBytes, StandardCharsets.UTF_8);
    }
}