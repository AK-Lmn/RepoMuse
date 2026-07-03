package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.ui.ProjectViewModel
import com.example.ui.ProjectViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectScreen(
    repository: ProjectRepository,
    projectId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel: ProjectViewModel = viewModel(factory = ProjectViewModelFactory(repository))
    val existingProject by viewModel.getProject(projectId).collectAsState(initial = null)
    
            var title by remember { mutableStateOf("") }
            var githubUrl by remember { mutableStateOf("") }
            var liveUrl by remember { mutableStateOf("") }
            var pitch by remember { mutableStateOf("") }
            var problem by remember { mutableStateOf("") }
            var features by remember { mutableStateOf("") }
            var techStack by remember { mutableStateOf("") }
            var challenges by remember { mutableStateOf("") }
            var resumeBullets by remember { mutableStateOf("") }
            var caseStudy by remember { mutableStateOf("") }
            var tags by remember { mutableStateOf("") }
            
            // Load existing data if editing
            LaunchedEffect(existingProject) {
                existingProject?.let {
                    title = it.title
                    githubUrl = it.githubUrl
                    liveUrl = it.liveUrl
                    pitch = it.pitch
                    problem = it.problem
                    features = it.features
                    techStack = it.techStack
                    challenges = it.challenges
                    resumeBullets = it.resumeBullets
                    caseStudy = it.caseStudy
                    tags = it.tags
                }
            }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == 0) "Add Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveProject(
                            Project(
                                id = projectId,
                                title = title,
                                githubUrl = githubUrl,
                                liveUrl = liveUrl,
                                pitch = pitch,
                                problem = problem,
                                features = features,
                                techStack = techStack,
                                challenges = challenges,
                                resumeBullets = resumeBullets,
                                caseStudy = caseStudy,
                                tags = tags
                            )
                        )
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Project")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var isGenerating by remember { mutableStateOf(false) }

            Button(
                onClick = { 
                    coroutineScope.launch {
                        isGenerating = true
                        
                        var extraContext = ""
                        if (githubUrl.isNotBlank()) {
                            val repoMetadata = com.example.data.GitHubService.getRepoMetadata(githubUrl)
                            if (repoMetadata.isNotBlank()) {
                                extraContext = "\nGitHub Repository Metadata:\n$repoMetadata"
                            }
                        }

                        val prompt = """
                            Analyze the following project information and generate missing details for a portfolio case study.
                            Project Title: $title
                            GitHub URL: $githubUrl
                            Live URL: $liveUrl
                            Current Notes: $features$extraContext
                            
                            Please format your response EXACTLY like this (use these exact labels):
                            PITCH:
                            <write a 1-2 sentence pitch>
                            PROBLEM:
                            <write a short paragraph about the problem this solves>
                            TECH_STACK:
                            <list the tech stack based on context or common defaults>
                            FEATURES:
                            <write 3-4 key features>
                            CHALLENGES:
                            <write a short paragraph about technical challenges>
                            RESUME_BULLETS:
                            <write 3 professional resume bullets>
                            CASE_STUDY:
                            <write a 2-paragraph portfolio case study>
                            TAGS:
                            <write 3-5 tags separated by commas, e.g., Web App, AI>
                        """.trimIndent()
                        
                        val aiResponse = com.example.data.GeminiService.generateContent(prompt)
                        
                        // Parse response
                        if (!aiResponse.startsWith("Error")) {
                            try {
                                val pitchMatch = Regex("PITCH:(.*?)(?=PROBLEM:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val problemMatch = Regex("PROBLEM:(.*?)(?=TECH_STACK:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val techMatch = Regex("TECH_STACK:(.*?)(?=FEATURES:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val featuresMatch = Regex("FEATURES:(.*?)(?=CHALLENGES:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val challengesMatch = Regex("CHALLENGES:(.*?)(?=RESUME_BULLETS:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val bulletsMatch = Regex("RESUME_BULLETS:(.*?)(?=CASE_STUDY:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val caseStudyMatch = Regex("CASE_STUDY:(.*?)(?=TAGS:)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                val tagsMatch = Regex("TAGS:(.*)", RegexOption.DOT_MATCHES_ALL).find(aiResponse)
                                
                                pitchMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) pitch = it }
                                problemMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) problem = it }
                                techMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) techStack = it }
                                featuresMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) features = it }
                                challengesMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) challenges = it }
                                bulletsMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) resumeBullets = it }
                                caseStudyMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) caseStudy = it }
                                tagsMatch?.groupValues?.get(1)?.trim()?.let { if (it.isNotBlank()) tags = it }
                            } catch (e: Exception) {
                                // Fallback
                                features = features + "\n\nAI Response:\n" + aiResponse
                            }
                        } else {
                            features = features + "\n\nAI Response:\n" + aiResponse
                        }
                        isGenerating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondary)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate with AI")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Project Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = { Text("GitHub URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = pitch,
                onValueChange = { pitch = it },
                label = { Text("Short Pitch") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = problem,
                onValueChange = { problem = it },
                label = { Text("Problem Solved") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = techStack,
                onValueChange = { techStack = it },
                label = { Text("Tech Stack (e.g., Next.js, React)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = features,
                onValueChange = { features = it },
                label = { Text("Key Features") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = challenges,
                onValueChange = { challenges = it },
                label = { Text("Challenges and Improvements") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            OutlinedTextField(
                value = resumeBullets,
                onValueChange = { resumeBullets = it },
                label = { Text("Resume Bullets") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = caseStudy,
                onValueChange = { caseStudy = it },
                label = { Text("Portfolio Case Study") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (e.g., Web App, AI)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
