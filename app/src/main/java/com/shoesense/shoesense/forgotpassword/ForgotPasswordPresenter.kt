import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.shoesense.shoesense.forgotpassword.ForgotPasswordView

class ForgotPasswordPresenter(private val view: ForgotPasswordView) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onBackClicked() {
        view.closeScreen()
    }

    fun onSubmitClicked(email: String) {
        if (email.isEmpty()) {
            view.showEmailError("Please enter your email address.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Please enter a valid email address.")
            return
        }

        Log.d("ForgotPasswordPresenter", "Attempting to send reset email to: $email")

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ForgotPasswordPresenter", "Reset email sent successfully.")
                    view.showResetLinkSent(email)
                } else {
                    val errorMessage = task.exception?.message ?: "Something went wrong. Please try again."
                    Log.e("ForgotPasswordPresenter", "Failed to send reset email: $errorMessage", task.exception)
                    view.showEmailError(errorMessage)
                }
            }
    }

    fun onDestroy() {
        // Clean up resources if needed later
    }
}