package com.mcnc.bizmob.plugin.base;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.mcnc.bizmob.core.plugin.BMCPlugin;

//import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class FingerPrintPlugin extends BMCPlugin {

    private final String TAG = FingerPrintPlugin.class.getSimpleName();

    JSONObject param = null;
    JSONObject resultData = null;
    String callback = null;
    String type = null;
    BiometricPrompt biometricPrompt = null;
    androidx.biometric.BiometricPrompt.PromptInfo promptInfo = null;

    /**
     * 지문인식 화면을 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-30                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 콜백 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        try {
            if (data.has("param")) {
                param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }
                if (param.has("type")) {
                    type = param.getString("type");
                } else {
                    type = "auth";
                }
            } else {
                type = "auth";
            }

            BiometricManager biometricManager = BiometricManager.from(getActivity());
            int enableValue = biometricManager.canAuthenticate();

            if (type.equals("available")) {
                if (enableValue == BiometricManager.BIOMETRIC_SUCCESS) {
                    resultData.put("result", true);
                    if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                } else {
                    resultData.put("result", false);
                    if (enableValue == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
                            || enableValue == BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
                        resultData.put("errCode", "BIO0001");
                    } else { // not supported
                        resultData.put("errCode", "BIO0004");
                    }
                    if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                }
            } else { // auth
                if (enableValue == BiometricManager.BIOMETRIC_SUCCESS) {
                    biometricPrompt = new BiometricPrompt(getFragment(), new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            // user cancel
                            try {
                                resultData.put("result", false);
                                resultData.put("errCode", "BIO0002");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            try {
                                resultData.put("result", true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            // authentication fail
                            try {
                                resultData.put("result", false);
                                resultData.put("errCode", "BIO0003");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                        }
                    });

                    BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        builder.setTitle("지문 인증")
                                .setSubtitle("Touch ID 혹은 비밀번호로 인증합니다.")
                                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                    } else {
                        builder.setTitle("지문 인증")
                                .setSubtitle("Touch ID 혹은 비밀번호로 인증합니다.")
                                .setDeviceCredentialAllowed(true);
                    }

                    promptInfo = builder.build();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            biometricPrompt.authenticate(promptInfo);
                        }
                    });
                } else {
                    resultData.put("result", false);
                    if (enableValue == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED || enableValue == BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
                        resultData.put("errCode", "BIO0001");
                    } else { // not supported
                        resultData.put("errCode", "BIO0004");
                    }
                    if (!TextUtils.isEmpty(callback)) listener.resultCallback("callback", callback, resultData);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
