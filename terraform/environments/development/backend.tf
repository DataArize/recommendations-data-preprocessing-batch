terraform {
  backend "gcs" {
    bucket = "terraform-recommendations-engine-state-bucket"
    prefix = "terraform/state"
  }
}