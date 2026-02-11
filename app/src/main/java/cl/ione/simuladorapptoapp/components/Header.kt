package cl.ione.simuladorapptoapp.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
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

    init {
        // Inflar el layout
        binding = ComponentHeaderBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        // Configurar atributos personalizados
        setupAttributes(attrs)

        // Configurar listeners
        setupListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.Header,
                0,
                0
            )

            try {
                // Obtener atributos desde XML
                val title = typedArray.getString(R.styleable.Header_title)
                val showBackButton = typedArray.getBoolean(R.styleable.Header_showBackButton, true)
                // Eliminamos showActionButton ya que no se usa

                // Para colores, usar valores seguros
                val backgroundColor = if (typedArray.hasValue(R.styleable.Header_backgroundColor)) {
                    typedArray.getColor(R.styleable.Header_backgroundColor, getDefaultBackgroundColor())
                } else {
                    getDefaultBackgroundColor()
                }

                val textColor = if (typedArray.hasValue(R.styleable.Header_textColor)) {
                    typedArray.getColor(R.styleable.Header_textColor, getDefaultTextColor())
                } else {
                    getDefaultTextColor()
                }

                // Configurar valores
                setTitle(title ?: "Título")
                setShowBackButton(showBackButton)
                setHeaderBackgroundColor(backgroundColor)
                setHeaderTextColor(textColor)

            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun getDefaultBackgroundColor(): Int {
        return ContextCompat.getColor(context, R.color.primary_red)
    }

    private fun getDefaultTextColor(): Int {
        return ContextCompat.getColor(context, android.R.color.white)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackClickListener?.invoke()
        }
        // Eliminamos el listener del botón de acción
    }

    // Métodos públicos para configurar el header

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun getTitle(): String = binding.tvTitle.text.toString()

    fun setShowBackButton(show: Boolean) {
        binding.btnBack.visibility = if (show) VISIBLE else GONE

        // Encuentra el Space por ID
        val spaceRight = findViewById<Space>(R.id.spaceRight)
        spaceRight?.visibility = if (show) VISIBLE else GONE
    }

    fun setHeaderBackgroundColor(color: Int) {
        binding.headerRoot.setBackgroundColor(color)
    }

    fun setHeaderTextColor(color: Int) {
        binding.tvTitle.setTextColor(color)
    }

    // Métodos para configurar listeners

    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    // Métodos para obtener referencias a las vistas
    fun getBackButton() = binding.btnBack
    fun getTitleView() = binding.tvTitle

    // Método para configurar completamente el header (simplificado)
    fun setup(
        title: String,
        showBackButton: Boolean = true,
        onBackClick: (() -> Unit)? = null
    ) {
        setTitle(title)
        setShowBackButton(showBackButton)

        if (onBackClick != null) {
            setOnBackClickListener(onBackClick)
        }
    }
}