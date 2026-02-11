package cl.ione.simuladorapptoapp.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ComponentFooterButtonsBinding
import com.google.android.material.button.MaterialButton

class FooterButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ComponentFooterButtonsBinding
    private var onPrimaryClickListener: (() -> Unit)? = null
    private var onSecondaryClickListener: (() -> Unit)? = null

    init {
        // Inflar el layout
        binding = ComponentFooterButtonsBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        // Configurar atributos personalizados
        setupAttributes(attrs)

        // Configurar listeners por defecto
        setupListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.FooterButtons
            )

            // Obtener textos desde XML
            val primaryText = typedArray.getString(R.styleable.FooterButtons_primaryText)
            val secondaryText = typedArray.getString(R.styleable.FooterButtons_secondaryText)

            // Configurar botones si tienen texto
            setPrimaryButton(primaryText)
            setSecondaryButton(secondaryText)

            typedArray.recycle()
        }
    }

    private fun setupListeners() {
        binding.btnPrimary.setOnClickListener {
            onPrimaryClickListener?.invoke()
        }

        binding.btnSecondary.setOnClickListener {
            onSecondaryClickListener?.invoke()
        }
    }

    // Función para configurar solo un botón
    fun setSingleButton(
        text: String,
        isPrimary: Boolean = true,
        onClickListener: () -> Unit = {}
    ) {
        if (isPrimary) {
            setPrimaryButton(text, onClickListener)
            binding.btnSecondary.visibility = GONE
        } else {
            setSecondaryButton(text, onClickListener)
            binding.btnPrimary.visibility = GONE
        }
    }

    // Función para configurar dos botones
    fun setButtons(
        primaryText: String? = null,
        secondaryText: String? = null,
        onPrimaryClick: (() -> Unit)? = null,
        onSecondaryClick: (() -> Unit)? = null
    ) {
        // Configurar botón primario
        if (primaryText != null) {
            setPrimaryButton(primaryText, onPrimaryClick)
        } else {
            binding.btnPrimary.visibility = GONE
        }

        // Configurar botón secundario
        if (secondaryText != null) {
            setSecondaryButton(secondaryText, onSecondaryClick)
        } else {
            binding.btnSecondary.visibility = GONE
        }

        // Ajustar layout si solo hay un botón
        adjustLayoutForSingleButton()
    }

    private fun setPrimaryButton(text: String?, onClickListener: (() -> Unit)? = null) {
        if (text.isNullOrEmpty()) {
            binding.btnPrimary.visibility = GONE
            return
        }

        binding.btnPrimary.apply {
            visibility = VISIBLE
            this.text = text
        }

        if (onClickListener != null) {
            onPrimaryClickListener = onClickListener
        }
    }

    private fun setSecondaryButton(text: String?, onClickListener: (() -> Unit)? = null) {
        if (text.isNullOrEmpty()) {
            binding.btnSecondary.visibility = GONE
            return
        }

        binding.btnSecondary.apply {
            visibility = VISIBLE
            this.text = text
        }

        if (onClickListener != null) {
            onSecondaryClickListener = onClickListener
        }
    }

    private fun adjustLayoutForSingleButton() {
        val primaryVisible = binding.btnPrimary.visibility == VISIBLE
        val secondaryVisible = binding.btnSecondary.visibility == VISIBLE

        if (primaryVisible && !secondaryVisible) {
            // Solo botón primario visible
            binding.btnPrimary.layoutParams = (binding.btnPrimary.layoutParams as LayoutParams).apply {
                weight = 0f
                width = LayoutParams.WRAP_CONTENT
                marginEnd = 0
            }
        } else if (!primaryVisible && secondaryVisible) {
            // Solo botón secundario visible
            binding.btnSecondary.layoutParams = (binding.btnSecondary.layoutParams as LayoutParams).apply {
                weight = 0f
                width = LayoutParams.WRAP_CONTENT
            }
        }
    }

    // Métodos para obtener referencias a los botones si necesitas personalizarlos más
    fun getPrimaryButton(): androidx.appcompat.widget.AppCompatButton = binding.btnPrimary
    fun getSecondaryButton(): Button = binding.btnSecondary

    // Método para cambiar colores dinámicamente
    fun setPrimaryButtonStyle(
        textColor: Int = R.color.primary_red,
        strokeColor: Int = R.color.primary_red,
        backgroundColor: Int = android.R.color.transparent
    ) {
        binding.btnPrimary.apply {
            setTextColor(context.getColor(textColor))
            setBackgroundColor(context.getColor(backgroundColor))
        }
    }

    fun setSecondaryButtonStyle(
        textColor: Int = android.R.color.white,
        backgroundColor: Int = R.color.primary_red
    ) {
        binding.btnSecondary.apply {
            setTextColor(context.getColor(textColor))
            backgroundTintList = context.getColorStateList(backgroundColor)
        }
    }
}