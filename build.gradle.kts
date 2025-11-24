
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

dependencies {
    val neiVersion: String by project
    implementation("com.github.GTNewHorizons:NotEnoughItems:$neiVersion:dev")

    val ae2Version: String by project;
    implementation("com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:$ae2Version:dev")

    val ocVersion: String by project;
    implementation("com.github.GTNewHorizons:OpenComputers:$ocVersion:dev")
}
