package sat.imme_login_v2;

public class usertoReceiverModel {
    private String idToken;
    private String image;
    private String otp;

    public String getmyOtp() {
        return otp;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getmyImage() {
        return image;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setmyImage(String image) {
        this.image = image;
    }

    public void setmyOtp(String otp) {
        this.otp = otp;
    }
}
