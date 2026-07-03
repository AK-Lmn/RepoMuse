package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectRepository
import com.example.ui.ProjectViewModel
import com.example.ui.ProjectViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    repository: ProjectRepository,
    projectId: String,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val viewModel: ProjectViewModel = viewModel(factory = ProjectViewModelFactory(repository))
    val project by viewModel.getProject(projectId).collectAsState(initial = null)
    val context = LocalContext.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            val currentProject = project
            if (currentProject != null) {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        com.example.utils.PdfExporter.exportToPdf(context, currentProject, outputStream)
                        Toast.makeText(context, "PDF Exported Successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val p = project
                    if (p != null) {
                        IconButton(onClick = {
                            val shareText = """
                                ${p.title}
                                
                                Pitch: ${p.pitch}
                                
                                Problem: ${p.problem}
                                
                                Tech Stack: ${p.techStack}
                                
                                Features:
                                ${p.features}
                                
                                Challenges:
                                ${p.challenges}
                                
                                Case Study:
                                ${p.caseStudy}
                            """.trimIndent()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Project Portfolio"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Portfolio")
                        }
                        IconButton(onClick = { onEditClick(projectId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Project")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (project == null) {
            Text("Loading...", modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val currentProject = project!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader("Pitch")
            Text(text = currentProject.pitch.ifBlank { "No pitch available." }, style = MaterialTheme.typography.bodyLarge)

            SectionHeader("Problem Solved")
            Text(text = currentProject.problem.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyLarge)

            SectionHeader("Tech Stack")
            Text(text = currentProject.techStack.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

            SectionHeader("Features")
            Text(text = currentProject.features.ifBlank { "No features listed." }, style = MaterialTheme.typography.bodyMedium)

            SectionHeader("Challenges and Improvements")
            Text(text = currentProject.challenges.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyMedium)
            
            SectionHeader("Portfolio Case Study")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentProject.caseStudy.ifBlank { "N/A" }, 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (currentProject.caseStudy.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val cleanTitle = currentProject.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
                                pdfLauncher.launch("RepoMuse_${cleanTitle}.pdf")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Case Study as PDF")
                        }
                    }
                }
            }

            if (currentProject.tags.isNotBlank()) {
                SectionHeader("Tags")
                Text(text = currentProject.tags, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            SectionHeader("Resume Bullets")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = currentProject.resumeBullets.ifBlank { "No bullets generated yet." }, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Resume Bullets", currentProject.resumeBullets)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy to Clipboard")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
    )
}
