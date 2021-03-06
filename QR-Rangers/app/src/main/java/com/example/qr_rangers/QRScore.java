package com.example.qr_rangers;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * Calculates the score of a given QR code
 * @author Ronan Sandoval
 * @version 1.0
 */
public class QRScore {

    QRScore() {}

    /**
     * Calculates the score of a given QR code
     *
     * @param qr
     *      QR Code object to obtain score of
     * @return
     *      Score of the given QR Code (long)
     */
    public int calculateScore(QRCode qr) {
        String sha256String = convertToSHA256(qr);
        return scoreSystem(sha256String);
    }

    /**
     * Obtains the SHA-256 hash of a given QR code
     *
     * @param qr
     *      QR Code object to obtain SHA-256 hash of
     * @return
     *      SHA-256 hash String of QR code
     */
    public String convertToSHA256(QRCode qr) {
        // TODO: Implement obtaining contents from QRCode
        String codeString = qr.toString();
        // https://www.baeldung.com/sha-256-hashing-java
        return Hashing.sha256()
                .hashString(codeString, StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Calculates the score of a QR Code converted to SHA-256 hash following the scoring system
     * proposed in the project description.
     *
     * @param sha256String
     *      SHA-256 hash String to obtain the score of
     * @return
     *      long value of the score of the SHA-256 hash
     */
    public int scoreSystem(String sha256String) {

        int score = 0;
        int comboLength = 0;
        int comboScore = 0;
        char comboChar = '\u0000';

        for (int i = 0; i < sha256String.length(); i++){
            char currentChar = sha256String.charAt(i);
            int charValue = Integer.parseInt(String.valueOf(currentChar), 16);

            if (currentChar == comboChar) {
                // currently in a combo!
                comboLength++;
                comboScore = (int) (charValue == 0 ? Math.pow(20, comboLength) : Math.pow(charValue, comboLength));
                if (i == sha256String.length() - 1) {
                    score += comboScore;
                }
            } else {
                // combo ended / started
                score += comboScore;
                comboLength = 0;
                comboChar = currentChar;
                comboScore = 0;
            }

        }

        score = score % 100000;
        score++;

        return score;
    }

}
