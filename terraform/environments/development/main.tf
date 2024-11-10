module "artifactory" {
  source = "../../modules/artifactory"
  project_id = var.project_id
  project_region = var.project_region
  repository_name = var.repository_name
}