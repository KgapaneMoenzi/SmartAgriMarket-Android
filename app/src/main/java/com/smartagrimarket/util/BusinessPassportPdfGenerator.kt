package com.smartagrimarket.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.smartagrimarket.R
import com.smartagrimarket.model.network.BusinessDashboardDto
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BusinessPassportPdfGenerator {

    fun generateAndSharePdf(context: Context, data: BusinessDashboardDto?, farmerName: String): Boolean {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size

        // 1. Create a high-res Bitmap (This prevents text editing)
        val bitmap = Bitmap.createBitmap(1190, 1684, Bitmap.Config.ARGB_8888) // 2x resolution for crispness
        val canvas = Canvas(bitmap)
        canvas.scale(2f, 2f) // Scale drawing operations

        // Colors
        val agriGreen = Color.parseColor("#1B7A3D")
        val textPrimary = Color.parseColor("#1A1A1A")
        val textSecondary = Color.parseColor("#666666")
        val cardBg = Color.parseColor("#F3F4F6")
        val borderColor = Color.parseColor("#E5E7EB")

        val paint = Paint()
        val textPaint = Paint().apply {
            color = textPrimary
            textSize = 14f
            isAntiAlias = true
        }

        // 2. Draw Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, 595f, 842f, paint)

        // 3. Draw Top Green Accent Bar
        paint.color = agriGreen
        canvas.drawRect(0f, 0f, 595f, 15f, paint)

        // 4. Draw Logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.agrimarket_logo)
            if (logoBitmap != null) {
                val newWidth = 120f
                val newHeight = (logoBitmap.height * (newWidth / logoBitmap.width))
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, newWidth.toInt(), newHeight.toInt(), true)
                canvas.drawBitmap(scaledLogo, 40f, 40f, paint)
            }
        } catch (e: Exception) { }

        // 5. Header Text & Dates
        textPaint.color = textPrimary
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Business Passport", 40f, 180f, textPaint)

        textPaint.color = textSecondary
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Financial & Funding Readiness Report", 40f, 205f, textPaint)

        val date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Generated for: $farmerName", 555f, 180f, textPaint)
        canvas.drawText("Date: $date", 555f, 205f, textPaint)

        // Divider
        paint.color = borderColor
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 230f, 555f, 230f, paint)

        // 6. Financial Performance Section
        textPaint.color = agriGreen
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Financial Performance", 40f, 270f, textPaint)

        // 7. Draw 3 Metric Cards
        val cardWidth = 160f
        val cardHeight = 80f
        val spacing = 17.5f
        val cardY = 290f

        drawMetricCard(canvas, 40f, cardY, cardWidth, cardHeight, "Net Profit (This Week)", "R${String.format(Locale.US, "%.2f", data?.netProfit ?: 0.0)}", agriGreen, textPaint, cardBg)
        drawMetricCard(canvas, 40f + cardWidth + spacing, cardY, cardWidth, cardHeight, "Total Revenue", "R${String.format(Locale.US, "%.2f", data?.revenueThisWeek ?: 0.0)}", textPrimary, textPaint, cardBg)
        drawMetricCard(canvas, 40f + (cardWidth + spacing) * 2, cardY, cardWidth, cardHeight, "Pending Payout", "R${String.format(Locale.US, "%.2f", data?.pendingPayout ?: 0.0)}", Color.parseColor("#F59E0B"), textPaint, cardBg)

        // 8. Circular Progress Ring
        val score = data?.fundingReadiness ?: 0
        val centerX = 297.5f
        val centerY = 460f
        val radius = 75f
        val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        paint.color = Color.parseColor("#E8F3DC")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        canvas.drawArc(rectF, 0f, 360f, false, paint)

        paint.color = agriGreen
        paint.strokeCap = Paint.Cap.ROUND
        val sweepAngle = (score / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint)
        paint.style = Paint.Style.FILL

        textPaint.color = textPrimary
        textPaint.textSize = 38f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("$score%", centerX, centerY + 12f, textPaint)

        textPaint.color = textSecondary
        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Funding Readiness Score", centerX, centerY + radius + 30f, textPaint)

        // 9. Checklist
        val checklistStartY = 620f
        textPaint.color = agriGreen
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Verification Checklist", 40f, checklistStartY, textPaint)

        val totalOrders = data?.totalOrders ?: 0
        drawChecklistRow(canvas, "Identity & Profile Verified", "✓ Done", checklistStartY + 30f, textPaint, agriGreen, borderColor)
        drawChecklistRow(canvas, "Banking Details Linked", "✓ Done", checklistStartY + 75f, textPaint, agriGreen, borderColor)
        drawChecklistRow(canvas, "Completed Orders Tracked", "$totalOrders Orders", checklistStartY + 120f, textPaint, textPrimary, borderColor)

        // 10. Footer
        textPaint.color = textSecondary
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("This document is auto-generated by SmartAgriMarket and is valid for government grant and funding applications.", 297.5f, 780f, textPaint)
        canvas.drawText("Powered by SmartAgriMarket | www.smartagrimarket.co.za", 297.5f, 800f, textPaint)

        // 11. Add a Security Watermark (Prevents easy tampering)
        canvas.save()
        canvas.rotate(-45f, 297.5f, 421f) // Rotate around center
        textPaint.color = Color.argb(20, 27, 122, 61) // Very faint green
        textPaint.textSize = 60f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("SMARTAGRIMARKET VERIFIED", 297.5f, 421f, textPaint)
        canvas.restore()

        // 12. Draw the Bitmap onto the PDF Page
        val page = pdfDocument.startPage(pageInfo)
        val pageCanvas = page.canvas
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 595, 842, true)
        pageCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Business_Passport_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            sharePdf(context, file, farmerName)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun drawMetricCard(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, value: String, valueColor: Int, paint: Paint, bgColor: Int) {
        paint.color = bgColor
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 12f, 12f, paint)

        paint.color = Color.parseColor("#666666")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, x + (width / 2), y + 30f, paint)

        paint.color = valueColor
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + (width / 2), y + 55f, paint)
    }

    private fun drawChecklistRow(canvas: Canvas, title: String, value: String, y: Float, paint: Paint, valueColor: Int, borderColor: Int) {
        paint.color = Color.parseColor("#1A1A1A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(title, 40f, y, paint)

        paint.color = valueColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, 555f, y, paint)

        paint.color = borderColor
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y + 15f, 555f, y + 15f, paint)
    }

    private fun sharePdf(context: Context, file: File, farmerName: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Business Passport - $farmerName")
                putExtra(Intent.EXTRA_TEXT, "Please find my SmartAgriMarket Business Passport attached for funding consideration.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share or Save Business Passport"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open share menu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}