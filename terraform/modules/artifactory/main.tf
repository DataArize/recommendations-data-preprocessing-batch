resource "google_artifact_registry_repository" "my-repo" {
  location      = var.project_region
  project       = var.project_id
  repository_id = var.repository_name
  format        = var.repository_format_docker
}