job("Build") {
    container(displayName = "Create issue on build fail", image = "gradle") {
        kotlinScript { api ->
            try {
                api.gradle("build")
            } catch (ex: Exception) {
                // get project Id
                val id = api.projectId()
                // get current build run number
                val runNumber = api.executionNumber()

                //get all issue statuses
                val statuses = api.space().projects.planning.issues.statuses.
                getAllIssueStatuses(project = ProjectIdentifier.Id(id))
                //get id of 'Open' issue status
                val openStatusId = statuses.find { it.name == "Open" }?.id
                    ?: throw kotlin.Exception("The 'Open' state doesn't exist in the project")
                // create issue with 'Open' status
                api.space().projects.planning.issues.createIssue(
                    project = ProjectIdentifier.Id(id),
                    // generate name based on build run number
                    title = "Job 'Build and publish' #$runNumber failed",
                    description = "${ex.message}",
                    status = openStatusId
                )
            }
        }
    }
}