package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.ui.ProjectViewModel
import com.example.ui.ProjectViewModelFactory
import kotlinx.coroutines.launch

private fun extractSection(response: String, sectionName: String, nextSections: List<String>): String {
    val lines = response.lines()
    var startLineIdx = -1
    
    // We search for a line matching our section header
    val escapedName = Regex.escape(sectionName)
    // Supports: PITCH, **Pitch:**, ## Pitch, ## Pitch:, **PITCH**:, etc.
    val headerRegex = Regex("^[#*_\\-\\s]*(?i)$escapedName[#*:*_\\-\\s]*$")
    
    for (i in lines.indices) {
        if (headerRegex.matches(lines[i].trim())) {
            startLineIdx = i
            break
        }
    }
    
    if (startLineIdx == -1) {
        // Fall back to fuzzy heading line matching
        for (i in lines.indices) {
            val trimmedLine = lines[i].trim()
            if (trimmedLine.startsWith("#") || trimmedLine.startsWith("*")) {
                val cleanedLine = trimmedLine.replace("[#*_:\\s\\-]".toRegex(), "")
                if (cleanedLine.equals(sectionName, ignoreCase = true) || cleanedLine.equals(sectionName.replace("_", ""), ignoreCase = true)) {
                    startLineIdx = i
                    break
                }
            }
        }
    }
    
    if (startLineIdx == -1) {
        // Final lookahead regex fallback
        val lookaheadPattern = if (nextSections.isNotEmpty()) {
            val nextSecsJoined = nextSections.joinToString("|")
            "(?=(?:\\n[#* ]*(?:$nextSecsJoined)[#*: ]*\\n?|$))"
        } else {
            "$"
        }
        val pattern = "(?i)(?:^|\\n)[#* ]*$sectionName[#*: ]*\\n?(.*?)$lookaheadPattern"
        val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(response)
        return match?.groupValues?.get(1)?.trim() ?: ""
    }
    
    // Find where the next section starts
    var endLineIdx = lines.size
    
    for (i in (startLineIdx + 1) until lines.size) {
        val line = lines[i].trim()
        var foundNextHeader = false
        for (nextSec in nextSections) {
            val nextHeaderRegex = Regex("^[#*_\\-\\s]*(?i)${Regex.escape(nextSec)}[#*:*_\\-\\s]*$")
            if (nextHeaderRegex.matches(line)) {
                foundNextHeader = true
                break
            }
        }
        if (foundNextHeader) {
            endLineIdx = i
            break
        }
    }
    
    val contentLines = lines.subList(startLineIdx + 1, endLineIdx)
    return contentLines.joinToString("\n").trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectScreen(
    repository: ProjectRepository,
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: ProjectViewModel = viewModel(factory = ProjectViewModelFactory(repository))
    
    // Only query if not a new project
    val existingProject by if (projectId != "new" && projectId != "0") {
        viewModel.getProject(projectId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf<Project?>(null) }
    }
    
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
    
    var showTitleError by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == "new" || projectId == "0") "Add Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                showTitleError = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Project Title is required before saving.")
                                }
                            } else {
                                viewModel.saveProject(
                                    Project(
                                        id = if (projectId == "new") "" else projectId,
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
                                Toast.makeText(context, "Project saved successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        enabled = !isGenerating // Safety: Disable save while AI is running
                    ) {
                        Icon(
                            Icons.Default.Save, 
                            contentDescription = "Save Project",
                            tint = if (isGenerating) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                                   else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            Button(
                onClick = { 
                    coroutineScope.launch {
                        isGenerating = true
                        
                        var extraContext = ""
                        if (githubUrl.isNotBlank()) {
                            try {
                                val repoMetadata = com.example.data.GitHubService.getRepoMetadata(githubUrl)
                                if (repoMetadata.isNotBlank()) {
                                    extraContext = "\nGitHub Repository Metadata:\n$repoMetadata"
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Could not fetch GitHub repository details. Generating from title only.")
                                    }
                                }
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("GitHub API error: ${e.localizedMessage}")
                                }
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
                                val parsedPitch = extractSection(aiResponse, "PITCH", listOf("PROBLEM", "TECH_STACK", "FEATURES", "CHALLENGES", "RESUME_BULLETS", "CASE_STUDY", "TAGS"))
                                val parsedProblem = extractSection(aiResponse, "PROBLEM", listOf("TECH_STACK", "FEATURES", "CHALLENGES", "RESUME_BULLETS", "CASE_STUDY", "TAGS"))
                                val parsedTechStack = extractSection(aiResponse, "TECH_STACK", listOf("FEATURES", "CHALLENGES", "RESUME_BULLETS", "CASE_STUDY", "TAGS"))
                                val parsedFeatures = extractSection(aiResponse, "FEATURES", listOf("CHALLENGES", "RESUME_BULLETS", "CASE_STUDY", "TAGS"))
                                val parsedChallenges = extractSection(aiResponse, "CHALLENGES", listOf("RESUME_BULLETS", "CASE_STUDY", "TAGS"))
                                val parsedResumeBullets = extractSection(aiResponse, "RESUME_BULLETS", listOf("CASE_STUDY", "TAGS"))
                                val parsedCaseStudy = extractSection(aiResponse, "CASE_STUDY", listOf("TAGS"))
                                val parsedTags = extractSection(aiResponse, "TAGS", emptyList())
                                
                                if (parsedPitch.isNotBlank() || parsedProblem.isNotBlank() || parsedCaseStudy.isNotBlank()) {
                                    if (parsedPitch.isNotBlank()) pitch = parsedPitch
                                    if (parsedProblem.isNotBlank()) problem = parsedProblem
                                    if (parsedTechStack.isNotBlank()) techStack = parsedTechStack
                                    if (parsedFeatures.isNotBlank()) features = parsedFeatures
                                    if (parsedChallenges.isNotBlank()) challenges = parsedChallenges
                                    if (parsedResumeBullets.isNotBlank()) resumeBullets = parsedResumeBullets
                                    if (parsedCaseStudy.isNotBlank()) caseStudy = parsedCaseStudy
                                    if (parsedTags.isNotBlank()) tags = parsedTags
                                } else {
                                    caseStudy = aiResponse
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("AI response format was non-standard. The text was placed into Case Study.")
                                    }
                                }
                            } catch (e: Exception) {
                                caseStudy = aiResponse
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("AI format parsing error. Full text saved to Case Study.")
                                }
                            }
                        } else {
                            val userFriendlyError = when {
                                aiResponse.contains("API Key is missing") -> "Gemini API Key is missing. Please configure it in the Secrets panel."
                                aiResponse.contains("404") -> "Error: Gemini API endpoint not found (404)."
                                aiResponse.contains("403") -> "Authentication error (403). Please verify your API Key."
                                aiResponse.contains("Unable to resolve host") || aiResponse.contains("timeout") -> "Network error. Please check your internet connection."
                                else -> "Failed to generate: " + aiResponse.removePrefix("Error: ")
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(userFriendlyError)
                            }
                        }
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), 
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generate with AI", fontWeight = FontWeight.Bold)
                }
            }

            if (isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "AI is analyzing your repository and writing a stellar case study...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Define reusable premium input text colors
            val inputColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )

            // Group 1: Project Identity Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Project Identity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            if (it.isNotBlank()) showTitleError = false
                        },
                        label = { Text("Project Title *") },
                        isError = showTitleError,
                        supportingText = {
                            if (showTitleError) {
                                Text("Project Title is required to save", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Provide a descriptive title for your case study")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    val isUrlInvalid = remember(githubUrl) {
                        githubUrl.isNotBlank() && !Regex("github\\.com/[^/]+/[^/]+").containsMatchIn(githubUrl.lowercase())
                    }

                    OutlinedTextField(
                        value = githubUrl,
                        onValueChange = { githubUrl = it },
                        label = { Text("GitHub URL") },
                        isError = isUrlInvalid,
                        supportingText = {
                            if (isUrlInvalid) {
                                Text("Please enter a valid GitHub repository URL (e.g., https://github.com/owner/repo)", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Format: https://github.com/owner/repo")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = liveUrl,
                        onValueChange = { liveUrl = it },
                        label = { Text("Live App URL (Optional)") },
                        supportingText = { Text("Link to your live production website/app") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isGenerating,
                        colors = inputColors
                    )
                }
            }

            // Group 2: Portfolio Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Generated Portfolio Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = pitch,
                        onValueChange = { pitch = it },
                        label = { Text("Short Pitch") },
                        supportingText = { Text("1-2 sentence compelling tagline") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = problem,
                        onValueChange = { problem = it },
                        label = { Text("Problem Solved") },
                        supportingText = { Text("What real-world frustration does this resolve?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = techStack,
                        onValueChange = { techStack = it },
                        label = { Text("Tech Stack (comma separated)") },
                        supportingText = { Text("e.g. Next.js, Jetpack Compose, FastAPI") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        label = { Text("Key Features") },
                        supportingText = { Text("Describe 3-4 features of this project") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = challenges,
                        onValueChange = { challenges = it },
                        label = { Text("Challenges and Improvements") },
                        supportingText = { Text("What technical bottlenecks did you conquer?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isGenerating,
                        colors = inputColors
                    )
                }
            }

            // Group 3: Resume Bullets & In-Depth Case Study Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Professional Assets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    OutlinedTextField(
                        value = resumeBullets,
                        onValueChange = { resumeBullets = it },
                        label = { Text("Resume Bullets") },
                        supportingText = { Text("Action-oriented resume/CV items to copy-paste") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = caseStudy,
                        onValueChange = { caseStudy = it },
                        label = { Text("Portfolio Case Study") },
                        supportingText = { Text("Full-length, narrative portfolio description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        maxLines = Int.MAX_VALUE, // Let it expand infinitely to make long text easy to edit!
                        enabled = !isGenerating,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        supportingText = { Text("e.g., Web App, AI, Open Source") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isGenerating,
                        colors = inputColors
                    )
                }
            }
        }
    }
}
