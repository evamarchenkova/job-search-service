package config

case class ApiClientConfig(
    baseUrl: String,
    vacanciesUrl: String,
    locationsUrl: String,
    professionalRolesUrl: String,
    dictionariesUrl: String
)
