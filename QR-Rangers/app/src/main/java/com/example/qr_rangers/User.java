package com.example.qr_rangers;

import java.util.ArrayList;

public class User {
    private String username;
    private ArrayList<QRCode> QRList;

    /**
     * Constructs a user object
     *
     * @param username The unique username of the user
     */
    User(String username) {
        // assert username is uniques
        this.username = username;
        QRList = new ArrayList<QRCode>();
    }

    /**
     * Adds a QR Code to the user's list
     *
     * @param code The QRCode that is to be added
     * @return Returns true if the add works, returns false if the QR Code already exists in the list, avoids duplicates
     */
    public boolean AddQR(QRCode code) {
        if (QRList.contains(code)) {
            return false;
        }
        QRList.add(code);
        return true;
    }

    /**
     * Removes a particular QRCode from the list
     *
     * @param code The QRCode we want to delete
     * @throws IllegalArgumentException If the QRCode does not exist in the list
     */
    public void DeleteQR(QRCode code) {
        if (QRList.contains(code)) {
            QRList.remove(code);
        } else { // Not sure why this would ever happen but
            throw new IllegalArgumentException();
        }
    }

    /**
     * Gets the sum of the scores of all QR Codes in the QR List
     *
     * @return
     *      Returns the sum of all of the scores in the QR List
     */
    public int getScoreSum() {
        int score = 0;
        for (QRCode code : QRList) {
            score += code.getScore();
        }
        return score;
    }

    /**
     * Gets the maximum score of all QR Codes in the QR List
     *
     * @return
     *      Returns the maximum of all of the scores in the QR List
     */
    public /*QRCode*/ int getScoreMax(){
        /*QRCode max_code = QRList.get(0); // maybe we could return the QR Code itself?
        for (QRCode code: QRList) {
            if(code.getScore() > max_code.getScore()){
                max_code = code;
            }
        }
        return max_code;*/
        int score = 0;
        for (QRCode code: QRList) {
            if(code.getScore() > score){
                score = code.getScore();
            }
        }
        return score;
    }

    /**
     * Gets the minimum score of all QR Codes in the QR List
     *
     * @return
     *      Returns the minimum of all of the scores in the QR List
     */
    public /*QRCode*/ int getScoreMin(){
        /*QRCode min_code = QRList.get(0); // maybe we could return the QR Code itself?
        for (QRCode code: QRList) {
            if(code.getScore() < min_code.getScore()){
                min_code = code;
            }
        }
        return min_code;*/
        if(getQRNum() > 0) {
            int score = Integer.MAX_VALUE;
            for (QRCode code : QRList) {
                if (code.getScore() < score) {
                    score = code.getScore();
                }
            }
            return score;
        }
        else{
            return 0; // idk what we want to return in these cases
        }
    }

    /**
     * Gets the number QR Codes in the QR List
     *
     * @return
     *      Returns the number of codes in the QR List
     */
    public int getQRNum(){
        return QRList.size();
    }
}
