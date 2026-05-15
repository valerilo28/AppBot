package com.example.escom_appcelular

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlin.collections.forEachIndexed

class ChatAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_USER = 1
    private val TYPE_BOT = 2
    private val TYPE_TYPING = 3

    override fun getItemViewType(position: Int): Int = when {
        messageList[position].isTyping -> TYPE_TYPING
        messageList[position].isBot -> TYPE_BOT
        else -> TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TYPING -> TypingViewHolder(
                inflater.inflate(R.layout.item_message_typing, parent, false)
            )
            TYPE_BOT -> BotViewHolder(
                inflater.inflate(R.layout.item_message_bot, parent, false)
            )
            else -> UserViewHolder(
                inflater.inflate(R.layout.item_message_user, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TypingViewHolder -> holder.bind()
            is BotViewHolder -> holder.bind(messageList[position])
            is UserViewHolder -> holder.bind(messageList[position])
        }
    }

    override fun getItemCount() = messageList.size

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder !is TypingViewHolder && holder.adapterPosition == messageList.size - 1) {
            holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
            )
        }
    }

    // ── ViewHolders ──────────────────────────────────────────────

    class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            listOf(R.id.dot1, R.id.dot2, R.id.dot3).forEachIndexed { i, id ->
                val dot = itemView.findViewById<View>(id)
                ObjectAnimator.ofFloat(dot, "translationY", 0f, -10f, 0f).apply {
                    duration = 500
                    startDelay = (i * 160).toLong()
                    repeatCount = ObjectAnimator.INFINITE
                    start()
                }
            }
        }
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val markwon: Markwon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(itemView.context))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(io.noties.markwon.core.CorePlugin.create())
            .build()

        fun bind(message: Message) {
            val textView = itemView.findViewById<TextView>(R.id.textMessageBot)
            textView.setTextIsSelectable(true)

            // Pre-procesar el texto para mejorar la presentación:
            // - Asegurar salto de línea antes de listas
            // - Limpiar asteriscos dobles sueltos
            val processed = preprocessMarkdown(message.text)
            markwon.setMarkdown(textView, processed)
        }

        /**
         * Mejora el markdown antes de renderizarlo:
         * - Agrega línea en blanco antes de listas para que Markwon las detecte
         * - Normaliza encabezados sin espacio (##Titulo → ## Titulo)
         */
        private fun preprocessMarkdown(text: String): String {
            return text
                .replace(Regex("(\\n)([-*]\\s)")) { "\n\n${it.groupValues[2]}" }
                .replace(Regex("(^|\\n)(#{1,3})([^\\s#])")) { "${it.groupValues[1]}${it.groupValues[2]} ${it.groupValues[3]}" }
                .trimEnd()
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.textMessageUser).text = message.text
        }
    }
}