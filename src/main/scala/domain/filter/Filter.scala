package domain.filter

import domain.filter.Types.{Experience, Location, ProfessionalRole}

case class Filter(
    prompt: Option[String],
    location: List[Location],
    professionalRole: List[ProfessionalRole],
    experience: Option[Experience],
    salary: Option[BigInt]
)
