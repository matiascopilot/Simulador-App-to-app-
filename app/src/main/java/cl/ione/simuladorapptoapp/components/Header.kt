package cl.ione.simuladorapptoapp.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ComponentHeaderBinding

class Header @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ComponentHeaderBinding
    private var onBackClickListener: (() -> Unit)? = null
    private var onRequestClickListener: (() -> Unit)? = null

    init {
        binding = ComponentHeaderBinding.inflate(LayoutInflater.from(context), this, true)
        setupAttributes(attrs)
        setupListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.Header, 0, 0)
            try {
                val title = typedArray.getString(R.styleable.Header_title) ?: "Título"
                val showBackButton = typedArray.getBoolean(R.styleable.Header_showBackButton, true)
                val showRequestButton = typedArray.getBoolean(R.styleable.Header_showRequestButton, true)

                setTitle(title)
                setShowBackButton(showBackButton)
                setShowRequestButton(showRequestButton)
                setHeaderBackgroundColor(ContextCompat.getColor(context, R.color.primary_red))
                setHeaderTextColor(ContextCompat.getColor(context, android.R.color.white))

            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { onBackClickListener?.invoke() }
        binding.btnRequest.setOnClickListener { onRequestClickListener?.invoke() }
    }

    fun setTitle(title: String) { binding.tvTitle.text = title }
    fun setShowBackButton(show: Boolean) { binding.btnBack.visibility = if (show) VISIBLE else GONE }
    fun setShowRequestButton(show: Boolean) { binding.btnRequest.visibility = if (show) VISIBLE else INVISIBLE  }
    fun setHeaderBackgroundColor(color: Int) { binding.headerRoot.setBackgroundColor(color) }
    fun setHeaderTextColor(color: Int) { binding.tvTitle.setTextColor(color) }
    fun setOnBackClickListener(listener: () -> Unit) { onBackClickListener = listener }
    fun setOnRequestClickListener(listener: () -> Unit) { onRequestClickListener = listener }

    fun showRequestJson(jsonString: String, title: String = "Request JSON") {
        RequestDialog.show(context, jsonString, title)
    }

    fun setup(
        title: String,
        showBackButton: Boolean = true,
        showRequestButton: Boolean = true,
        onBackClick: (() -> Unit)? = null,
        onRequestClick: (() -> Unit)? = null
    ) {
        setTitle(title)
        setShowBackButton(showBackButton)
        setShowRequestButton(showRequestButton)
        onBackClick?.let { setOnBackClickListener(it) }
        onRequestClick?.let { setOnRequestClickListener(it) }
    }
}