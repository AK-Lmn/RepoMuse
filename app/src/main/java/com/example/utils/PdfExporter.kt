package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.Project
import java.io.OutputStream

object PdfExporter {
    
    fun exportToPdf(context: Context, project: Project, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        
        // Standard US Letter dimensions: 612 x 792 points (72 points per inch)
        val pageWidth = 612
        val pageHeight = 792
        val margin = 54 // 0.75 in margins
        val contentWidth = pageWidth - (2 * margin)
        
        var pageNumber = 1
        var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas
        
        // Setup Paints
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#0F172A") // Slate 900
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val headingPaint = TextPaint().apply {
            color = Color.parseColor("#2563EB") // Primary blue
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#334155") // Slate 700
            textSize = 10f
            isAntiAlias = true
        }
        
        val italicBodyPaint = TextPaint().apply {
            color = Color.parseColor("#475569") // Slate 600
            textSize = 10f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        }
        
        val metaPaint = TextPaint().apply {
            color = Color.parseColor("#64748B") // Slate 500
            textSize = 9f
            isAntiAlias = true
        }

        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8") // Slate 400
            textSize = 8f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0") // Slate 200
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val accentLinePaint = Paint().apply {
            color = Color.parseColor("#2563EB") // Primary Blue
            strokeWidth = 3f
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
        }
        
        var yPosition = margin.toFloat()

        fun drawFooter(c: Canvas, pageNum: Int) {
            c.drawLine(margin.toFloat(), pageHeight - 40f, (pageWidth - margin).toFloat(), pageHeight - 40f, linePaint)
            c.drawText("RepoMuse Case Study Generator — ${project.title}", margin.toFloat(), pageHeight - 25f, footerPaint)
            val pageStr = "Page $pageNum"
            val pageStrWidth = footerPaint.measureText(pageStr)
            c.drawText(pageStr, pageWidth - margin.toFloat() - pageStrWidth, pageHeight - 25f, footerPaint)
        }

        fun startNewPage() {
            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber++
            currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(currentPageInfo)
            canvas = currentPage.canvas
            yPosition = margin.toFloat()
        }

        fun drawSection(title: String, content: String, isItalic: Boolean = false) {
            if (content.isBlank()) return
            
            // Check if heading fits. If not, start new page.
            val headingSpacing = 20f
            val paragraphSpacing = 8f
            val spaceNeeded = headingSpacing + 25f // heading + initial space
            if (yPosition + spaceNeeded > pageHeight - 60) {
                startNewPage()
            }
            
            // Draw section heading
            canvas.drawText(title.uppercase(), margin.toFloat(), yPosition + 15f, headingPaint)
            yPosition += 22f
            
            // Draw heading underline decoration
            val textWidth = headingPaint.measureText(title.uppercase())
            canvas.drawLine(margin.toFloat(), yPosition, margin.toFloat() + textWidth, yPosition, linePaint)
            yPosition += 8f

            // Prepare content text
            val paintToUse = if (isItalic) italicBodyPaint else bodyPaint
            
            val staticLayout = StaticLayout(
                content,
                paintToUse,
                contentWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                0f,
                false
            )
            
            // Draw static layout line-by-line, checking for page boundary
            val linesCount = staticLayout.lineCount
            for (i in 0 until linesCount) {
                val lineBottom = staticLayout.getLineBottom(i)
                val lineTop = staticLayout.getLineTop(i)
                val lineHeight = lineBottom - lineTop
                
                if (yPosition + lineHeight > pageHeight - 60) {
                    startNewPage()
                }
                
                canvas.save()
                canvas.translate(margin.toFloat(), yPosition)
                
                val startChar = staticLayout.getLineStart(i)
                val endChar = staticLayout.getLineEnd(i)
                val lineText = content.substring(startChar, endChar)
                canvas.drawText(lineText, 0f, -staticLayout.getLineAscent(i).toFloat(), paintToUse)
                canvas.restore()
                
                yPosition += lineHeight
            }
            
            yPosition += paragraphSpacing + 10f
        }

        // 1. Draw Header on First Page
        val titleLayout = StaticLayout(
            project.title.ifBlank { "Untitled Project" },
            titlePaint,
            contentWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0f,
            false
        )
        
        titleLayout.draw(canvas)
        yPosition += titleLayout.height + 10f
        
        // Decorative bar under title
        canvas.drawRect(
            margin.toFloat(),
            yPosition,
            margin.toFloat() + 80f,
            yPosition + 4f,
            accentLinePaint
        )
        yPosition += 15f
        
        // Metadata (GitHub, Live URL, Tags)
        val metadataBuilder = StringBuilder()
        if (project.githubUrl.isNotBlank()) metadataBuilder.append("GitHub: ${project.githubUrl}\n")
        if (project.liveUrl.isNotBlank()) metadataBuilder.append("Live Project: ${project.liveUrl}\n")
        if (project.tags.isNotBlank()) metadataBuilder.append("Tags: ${project.tags}\n")
        
        if (metadataBuilder.isNotBlank()) {
            val metaLayout = StaticLayout(
                metadataBuilder.toString().trim(),
                metaPaint,
                contentWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.2f,
                0f,
                false
            )
            metaLayout.draw(canvas)
            yPosition += metaLayout.height + 20f
        }
        
        // Draw each section
        drawSection("The Pitch", project.pitch, isItalic = true)
        drawSection("Problem Solved", project.problem)
        drawSection("Tech Stack", project.techStack)
        drawSection("Key Features", project.features)
        drawSection("Challenges and Improvements", project.challenges)
        drawSection("Portfolio Case Study", project.caseStudy)
        drawSection("Resume Bullets", project.resumeBullets)
        
        // Finish the final page
        drawFooter(canvas, pageNumber)
        pdfDocument.finishPage(currentPage)
        
        // Write out to output stream
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
