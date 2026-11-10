package com.abwallet.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val navy = Color.rgb(7, 20, 38)
    private val panel = Color.rgb(14, 34, 58)
    private val blue = Color.rgb(40, 144, 245)
    private val cyan = Color.rgb(64, 205, 232)
    private val white = Color.WHITE
    private val muted = Color.rgb(157, 177, 199)

    private var balance = 12450.75
    private val chartValues = mutableListOf(7600f, 8200f, 7900f, 9100f, 8750f, 10300f, 12450f)
    private val transactions = mutableListOf(
        Tx("راتب / دخل تجريبي", "+ 4,500.00 ج.م", "اليوم • 10:30 ص", true),
        Tx("مشتريات تجريبية", "- 650.00 ج.م", "أمس • 08:15 م", false),
        Tx("إضافة رصيد تجريبي", "+ 2,000.00 ج.م", "الأحد • 01:20 م", true)
    )
    private var googleClient: GoogleSignInClient? = null
    private var currentEmail = "وضع الاستعراض"

    data class Tx(val title: String, val amount: String, val date: String, val incoming: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy
        showLogin()
    }

    private fun showLogin() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(26), dp(30), dp(26), dp(30))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(5, 15, 31), Color.rgb(9, 37, 68), Color.rgb(7, 20, 38)))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val mark = TextView(this).apply {
            text = "AB"
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(blue, 24)
        }
        root.addView(mark, LinearLayout.LayoutParams(dp(82), dp(82)).apply { bottomMargin = dp(22) })
        root.addView(label("AB Wallet", 30f, white, true, Gravity.CENTER), full().apply { bottomMargin = dp(8) })
        root.addView(label("محفظتك الرقمية بطريقة أبسط", 16f, muted, false, Gravity.CENTER), full().apply { bottomMargin = dp(38) })

        val google = label("G   المتابعة باستخدام Google", 16f, Color.rgb(30, 43, 57), true, Gravity.CENTER).apply {
            background = rounded(white, 16)
            setPadding(dp(14), dp(17), dp(14), dp(17))
            setOnClickListener { startGoogleSignIn() }
        }
        root.addView(google, full().apply { bottomMargin = dp(14) })

        val preview = label("استعراض الواجهة التجريبية", 15f, white, true, Gravity.CENTER).apply {
            background = rounded(Color.rgb(24, 65, 107), 16)
            setPadding(dp(14), dp(16), dp(14), dp(16))
            setOnClickListener {
                currentEmail = "وضع الاستعراض — بدون تسجيل دخول"
                showDashboard()
            }
        }
        root.addView(preview, full().apply { bottomMargin = dp(28) })
        root.addView(label("نسخة تجريبية فقط • لا تستقبل أو تحتفظ بأموال حقيقية", 12f, muted, false, Gravity.CENTER), full())
        setContentView(root)
    }

    private fun startGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id).trim()
        if (webClientId.isBlank() || webClientId.startsWith("YOUR_")) {
            Toast.makeText(this, "زر Google جاهز، لكن يلزم إضافة Web Client ID لتفعيل تسجيل الدخول.", Toast.LENGTH_LONG).show()
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        googleClient = GoogleSignIn.getClient(this, options)
        @Suppress("DEPRECATION")
        startActivityForResult(googleClient!!.signInIntent, 9001)
    }

    @Deprecated("Google Sign-In callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                currentEmail = account.email ?: "حساب Google"
                showDashboard()
            } catch (e: ApiException) {
                Toast.makeText(this, "تعذر تسجيل الدخول إلى Google. راجع إعدادات OAuth.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDashboard() {
        val scroll = ScrollView(this).apply { setBackgroundColor(navy) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(26))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        scroll.addView(page)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        brand.addView(label("AB Wallet", 23f, white, true))
        brand.addView(label(currentEmail, 11f, muted, false).apply { maxLines = 1 })
        header.addView(brand)
        header.addView(label("خروج", 13f, cyan, true, Gravity.CENTER).apply {
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.rgb(17, 47, 78), 12)
            setOnClickListener {
                googleClient?.signOut()
                currentEmail = "وضع الاستعراض"
                showLogin()
            }
        })
        page.addView(header, full().apply { bottomMargin = dp(20) })

        val notice = label("وضع تجريبي — الرصيد والعمليات افتراضية وليست أموالًا حقيقية", 12f, Color.rgb(255, 220, 150), false)
        notice.setPadding(dp(12), dp(11), dp(12), dp(11))
        notice.background = rounded(Color.rgb(66, 49, 24), 12)
        page.addView(notice, full().apply { bottomMargin = dp(16) })

        val balanceCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(21, 117, 210), Color.rgb(14, 67, 126), Color.rgb(12, 48, 91))).apply { cornerRadius = dp(22).toFloat() }
        }
        balanceCard.addView(label("الرصيد المتاح للتجربة", 14f, Color.rgb(221, 238, 255), false))
        balanceCard.addView(label("${money(balance)} ج.م", 31f, white, true).apply { setPadding(0, dp(10), 0, dp(6)) })
        balanceCard.addView(label("••••  ••••  ••••  2048", 13f, Color.rgb(205, 229, 255), false))
        page.addView(balanceCard, full().apply { bottomMargin = dp(18) })

        page.addView(label("الخدمات السريعة", 18f, white, true), full().apply { bottomMargin = dp(10) })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(actionTile("＋", "إيداع", "إضافة افتراضية") { amountDialog("إيداع تجريبي", true) }, weighted())
        actions.addView(actionTile("↗", "سحب", "خصم افتراضي") { amountDialog("سحب تجريبي", false) }, weighted().apply { marginStart = dp(8) })
        actions.addView(actionTile("⇄", "تحويل", "تحويل افتراضي") { amountDialog("تحويل تجريبي", false, true) }, weighted().apply { marginStart = dp(8) })
        page.addView(actions, full().apply { bottomMargin = dp(20) })

        val chartCard = card()
        chartCard.addView(label("تحليل الرصيد", 18f, white, true), full().apply { bottomMargin = dp(4) })
        chartCard.addView(label("حركة افتراضية خلال آخر 7 أيام", 12f, muted, false), full().apply { bottomMargin = dp(8) })
        chartCard.addView(WalletChartView(this), LinearLayout.LayoutParams(-1, dp(150)))
        page.addView(chartCard, full().apply { bottomMargin = dp(16) })

        val paymentCard = card()
        paymentCard.addView(label("طرق الدفع", 18f, white, true), full().apply { bottomMargin = dp(12) })
        paymentCard.addView(paymentRow("بطاقة بنكية", "Visa / Mastercard", "غير مفعّلة") { paymentInfo() })
        paymentCard.addView(paymentRow("محفظة هاتف", "محافظ الهاتف المحمول", "غير مفعّلة") { paymentInfo() })
        paymentCard.addView(paymentRow("تحويل بنكي", "تحويل من بنك إلى بنك", "غير مفعّل") { paymentInfo() })
        page.addView(paymentCard, full().apply { bottomMargin = dp(16) })

        val txCard = card()
        txCard.addView(label("آخر العمليات", 18f, white, true), full().apply { bottomMargin = dp(10) })
        transactions.take(8).forEachIndexed { index, tx ->
            txCard.addView(transactionRow(tx))
            if (index != transactions.take(8).lastIndex) {
                val divider = View(this).apply { setBackgroundColor(Color.rgb(36, 58, 81)) }
                txCard.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(10); bottomMargin = dp(10) })
            }
        }
        page.addView(txCard, full().apply { bottomMargin = dp(18) })

        page.addView(label("AB Wallet • إصدار تجريبي 1.0", 11f, muted, false, Gravity.CENTER), full())
        setContentView(scroll)
    }

    private fun actionTile(symbol: String, title: String, subtitle: String, action: () -> Unit): LinearLayout {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(14), dp(6), dp(14))
            background = rounded(panel, 16)
            setOnClickListener { action() }
        }
        tile.addView(label(symbol, 25f, cyan, true, Gravity.CENTER))
        tile.addView(label(title, 13f, white, true, Gravity.CENTER).apply { setPadding(0, dp(5), 0, dp(2)) })
        tile.addView(label(subtitle, 9f, muted, false, Gravity.CENTER))
        return tile
    }

    private fun paymentRow(title: String, detail: String, status: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
            setOnClickListener { action() }
        }
        val icon = label("◇", 22f, cyan, true, Gravity.CENTER).apply {
            background = rounded(Color.rgb(22, 55, 86), 12)
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginStart = dp(10) })
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        labels.addView(label(title, 14f, white, true))
        labels.addView(label(detail, 11f, muted, false))
        row.addView(labels)
        row.addView(label(status, 10f, Color.rgb(255, 202, 120), true, Gravity.CENTER))
        return row
    }

    private fun transactionRow(tx: Tx): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val icon = label(if (tx.incoming) "↓" else "↑", 20f, if (tx.incoming) cyan else Color.rgb(255, 150, 150), true, Gravity.CENTER)
        icon.background = rounded(Color.rgb(23, 53, 79), 12)
        row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginStart = dp(10) })
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        body.addView(label(tx.title, 13f, white, true))
        body.addView(label(tx.date, 10f, muted, false))
        row.addView(body)
        row.addView(label(tx.amount, 13f, if (tx.incoming) cyan else Color.rgb(255, 160, 160), true))
        return row
    }

    private fun amountDialog(title: String, incoming: Boolean, transfer: Boolean = false) {
        val input = EditText(this).apply {
            hint = "المبلغ بالجنيه المصري"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
            textSize = 16f
        }
        val wrap = LinearLayout(this).apply { setPadding(dp(22), dp(8), dp(22), 0); addView(input, full()) }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("هذه عملية افتراضية للتجربة فقط.")
            .setView(wrap)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("تأكيد تجريبي") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    Toast.makeText(this, "اكتب مبلغًا صحيحًا.", Toast.LENGTH_SHORT).show()
                } else if (!incoming && amount > balance) {
                    Toast.makeText(this, "الرصيد التجريبي غير كافٍ.", Toast.LENGTH_SHORT).show()
                } else {
                    balance += if (incoming) amount else -amount
                    val titleText = when {
                        incoming -> "إيداع تجريبي"
                        transfer -> "تحويل تجريبي"
                        else -> "سحب تجريبي"
                    }
                    val prefix = if (incoming) "+ " else "- "
                    val date = SimpleDateFormat("dd/MM • hh:mm a", Locale.getDefault()).format(Date())
                    transactions.add(0, Tx(titleText, prefix + money(amount) + " ج.م", date, incoming))
                    chartValues.add(balance.toFloat())
                    if (chartValues.size > 7) chartValues.removeAt(0)
                    showDashboard()
                }
            }.show()
    }

    private fun paymentInfo() {
        AlertDialog.Builder(this)
            .setTitle("طرق الدفع")
            .setMessage("طرق الدفع معروضة للتصميم فقط وغير متصلة بأي بنك أو شركة دفع. تفعيل الأموال الحقيقية يتطلب مزود خدمة مرخصًا وربطًا آمنًا من جهة الخادم.")
            .setPositiveButton("تمام", null)
            .show()
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(panel, 18)
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean, gravity: Int = Gravity.START): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            this.gravity = gravity
            if (bold) typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = true
            textDirection = View.TEXT_DIRECTION_LOCALE
        }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun full() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun weighted() = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun money(value: Double): String = String.format(Locale.US, "%,.2f", value)

    private inner class WalletChartView(context: Context) : View(context) {
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(38, 63, 88); strokeWidth = dp(1).toFloat() }
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cyan; strokeWidth = dp(3).toFloat(); style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white; style = Paint.Style.FILL }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textSize = dp(9).toFloat() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val left = dp(10).toFloat()
            val right = width - dp(10).toFloat()
            val top = dp(12).toFloat()
            val bottom = height - dp(24).toFloat()
            for (i in 0..3) {
                val y = top + (bottom - top) * i / 3f
                canvas.drawLine(left, y, right, y, gridPaint)
            }
            val values = chartValues.takeLast(7)
            if (values.size < 2) return
            val min = (values.minOrNull() ?: 0f) * 0.92f
            val max = (values.maxOrNull() ?: 1f) * 1.06f
            val range = (max - min).coerceAtLeast(1f)
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = left + (right - left) * index / (values.size - 1).toFloat()
                val y = bottom - ((value - min) / range) * (bottom - top)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                canvas.drawCircle(x, y, dp(3).toFloat(), dotPaint)
            }
            canvas.drawPath(path, linePaint)
            val labels = listOf("منذ 6 أيام", "منذ 3 أيام", "اليوم")
            canvas.drawText(labels[0], left, height - dp(5).toFloat(), textPaint)
            canvas.drawText(labels[1], (left + right) / 2 - dp(22), height - dp(5).toFloat(), textPaint)
            canvas.drawText(labels[2], right - dp(23), height - dp(5).toFloat(), textPaint)
        }
    }
}
