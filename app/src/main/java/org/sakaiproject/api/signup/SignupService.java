package org.sakaiproject.api.signup;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.sakaiproject.sakai.AppController;
import org.sakaiproject.api.login.LoginService;
import org.sakaiproject.general.Actions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vasilis on 10/17/15.
 * Create new user to sakai
 */
public class SignupService {

    String userId;
    private final String tag_sign_up = "sign up";
    private final String tag_eid_exists = "userEid exists";
    private boolean exists = true;

    private Context context;

    private ProgressBar userExistsProgressBar, signupProgressBar;
    private ImageView userExistsImageView;


    public SignupService(Context context, ImageView userExistsImageView, ProgressBar signupProgressBar, ProgressBar userExistsProgressBar) {
        this.context = context;
        this.userExistsImageView = userExistsImageView;
        this.signupProgressBar = signupProgressBar;
        this.userExistsProgressBar = userExistsProgressBar;
    }

    /**
     * the REST call (POST) for the signup
     *
     * @param url       the url
     * @param eid       the username
     * @param firstName the first name
     * @param lastName  the last name
     * @param email     the email
     * @param password  the password
     */
    public void signUp(final String url, final String eid, final String firstName, final String lastName, final String email, final String password) {

        StringRequest signupRequest = new StringRequest(Request.Method.POST, url + "user", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                userId = response;
                //new LoginService(signupProgressBar, context).login(url, eid, password);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                signupProgressBar.setVisibility(View.GONE);
                VolleyLog.d(tag_sign_up, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("eid", eid);
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("email", email);
                params.put("password", password);
                params.put("type", "registered");
                return params;
            }
        };

        signupRequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(signupRequest, tag_sign_up);
    }

    /**
     * check if the username already exists
     *
     * @param url
     */
    public void eidExists(String url, final EidExistence existence) {

        StringRequest existsRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                exists = false;
                userExistsImageView.setVisibility(View.VISIBLE);
                userExistsProgressBar.setVisibility(View.GONE);
                userExistsImageView.setImageDrawable(Actions.selectValidationImage(context, exists));
                existence.signUpButton(exists);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                exists = true;
                userExistsImageView.setVisibility(View.VISIBLE);
                userExistsProgressBar.setVisibility(View.GONE);
                userExistsImageView.setImageDrawable(Actions.selectValidationImage(context, exists));
                existence.signUpButton(exists);
            }
        });


        existsRequest.setShouldCache(false);

        AppController.getInstance().addToRequestQueue(existsRequest, tag_eid_exists);
    }
}
