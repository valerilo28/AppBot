package com.example.escom_appcelular

/**
 * Mapa curricular de ISC 2020.
 * Las claves del mapa son los nombres normalizados a MAYÚSCULAS
 * para coincidir con el campo "materias" del profesores.json.
 */
object CurriculumData {

    data class Semestre(val numero: Int, val materias: List<String>)

    val ISC: List<Semestre> = listOf(
        Semestre(1, listOf(
            "CALCULO",
            "ANALISIS VECTORIAL",
            "MATEMATICAS DISCRETAS",
            "COMUNICACION ORAL Y ESCRITA",
            "FUNDAMENTOS DE PROGRAMACION"
        )),
        Semestre(2, listOf(
            "ALGEBRA LINEAL",
            "CALCULO APLICADO",
            "MECANICA Y ELECTROMAGNETISMO",
            "INGENIERIA, ETICA Y SOCIEDAD",
            "FUNDAMENTOS ECONOMICOS",
            "ALGORITMOS Y ESTRUCTURAS DE DATOS"
        )),
        Semestre(3, listOf(
            "ECUACIONES DIFERENCIALES",
            "CIRCUITOS ELECTRICOS",
            "FUNDAMENTOS DE DISEÑO DIGITAL",
            "BASES DE DATOS",
            "FINANZAS EMPRESARIALES",
            "PARADIGMAS DE PROGRAMACION",
            "ANALISIS Y DISEÑO DE ALGORITMOS"
        )),
        Semestre(4, listOf(
            "PROBABILIDAD Y ESTADISTICA",
            "MATEMATICAS AVANZADAS PARA LA INGENIERIA",
            "ELECTRONICA ANALOGICA",
            "DISEÑO DE SISTEMAS DIGITALES",
            "TECNOLOGIAS PARA EL DESARROLLO DE APLICACIONES WEB",
            "SISTEMAS OPERATIVOS",
            "TEORIA DE LA COMPUTACION"
        )),
        Semestre(5, listOf(
            "PROCESAMIENTO DIGITAL DE SEÑALES",
            "INSTRUMENTACION Y CONTROL",
            "ARQUITECTURA DE COMPUTADORAS",
            "ANALISIS Y DISEÑO DE SISTEMAS",
            "FORMULACION Y EVALUACION DE PROYECTOS INFORMATICOS",
            "COMPILADORES",
            "REDES DE COMPUTADORAS"
        )),
        Semestre(6, listOf(
            "SISTEMAS EN CHIP",
            "METODOS CUANTITATIVOS PARA LA TOMA DE DECISIONES",
            "INGENIERIA DE SOFTWARE",
            "INTELIGENCIA ARTIFICIAL",
            "APLICACIONES PARA COMUNICACIONES EN RED"
        )),
        Semestre(7, listOf(
            "DESARROLLO DE APLICACIONES MOVILES NATIVAS",
            "TRABAJO TERMINAL I",
            "SISTEMAS DISTRIBUIDOS",
            "ADMINISTRACION DE SERVICIOS EN RED"
        )),
        Semestre(8, listOf(
            "ESTANCIA PROFESIONAL",
            "DESARROLLO DE HABILIDADES SOCIALES PARA LA ALTA DIRECCION",
            "TRABAJO TERMINAL II",
            "GESTION EMPRESARIAL",
            "LIDERAZGO PERSONAL"
        ))
    )

    /** Optativas ISC — en inglés tal como vienen en el JSON */
    val ISC_OPTATIVAS: List<String> = listOf(
        "COMPUTER GRAPHICS",
        "COMPUTER SECURITY",
        "GENETIC ALGORITHMS",
        "INTRODUCTION TO CRYPTOGRAPHY",
        "MACHINE LEARNING",
        "SOFTWARE QUALITY ASSURANCE AND DESIGN PATTERNS",
        "BIOINFORMATICS",
        "IT GOVERNANCE",
        "NATURAL LANGUAGE PROCESSING",
        "SELECTED TOPICS OF CRYPTOGRAPHY",
        "VIRTUAL AND AUGMENTED REALITY",
        "WEB CLIENT AND BACKEND DEVELOPMENT FRAMEWORKS",
        "BIG DATA",
        "COMPLEX SYSTEMS",
        "COMPUTING SELECTED TOPICS I",
        "COMPUTING SELECTED TOPICS II",
        "DATA MINING",
        "ECONOMIC ENGINEERING",
        "IMAGE ANALYSIS",
        "INTERNET OF THINGS",
        "VIRTUAL INSTRUMENTATION",
        "VIRTUAL INSTRUMENTATION APPLICATIONS",
        "CELLULAR AUTOMATA",
        "EMBEDDED SYSTEMS",
        "HIGH TECHNOLOGY ENTERPRISE MANAGEMENT",
        "NON RELATIONAL DATABASES",
        "STATISTICAL TOOLS FOR DATA ANALYTICS"
    )

    // ─── IIA ─────────────────────────────────────────────────────

    val IIA: List<Semestre> = listOf(
        Semestre(1, listOf(
            "FUNDAMENTOS DE PROGRAMACION",
            "MATEMATICAS DISCRETAS",
            "CALCULO",
            "COMUNICACION ORAL Y ESCRITA",
            "MECANICA Y ELECTROMAGNETISMO",
            "FUNDAMENTOS ECONOMICOS"
        )),
        Semestre(2, listOf(
            "ALGORITMOS Y ESTRUCTURAS DE DATOS",
            "FUNDAMENTOS DE DISEÑO DIGITAL",
            "CALCULO MULTIVARIABLE",
            "INGENIERIA, ETICA Y SOCIEDAD",
            "ALGEBRA LINEAL",
            "FINANZAS EMPRESARIALES"
        )),
        Semestre(3, listOf(
            "ANALISIS Y DISEÑO DE ALGORITMOS",
            "PARADIGMAS DE PROGRAMACION",
            "ECUACIONES DIFERENCIALES",
            "BASES DE DATOS",
            "DISEÑO DE SISTEMAS DIGITALES",
            "LIDERAZGO PERSONAL"
        )),
        Semestre(4, listOf(
            "FUNDAMENTOS DE INTELIGENCIA ARTIFICIAL",
            "PROBABILIDAD Y ESTADISTICA",
            "MATEMATICAS AVANZADAS PARA LA INGENIERIA",
            "TECNOLOGIAS PARA EL DESARROLLO DE APLICACIONES WEB",
            "ANALISIS Y DISEÑO DE SISTEMAS",
            "PROCESAMIENTO DIGITAL DE IMAGENES"
        )),
        Semestre(5, listOf(
            "APRENDIZAJE DE MAQUINA",
            "VISION ARTIFICIAL",
            "TEORIA DE LA COMPUTACION",
            "PROCESAMIENTO DE SEÑALES",
            "ALGORITMOS BIOINSPIRADOS",
            "TECNOLOGIAS DE LENGUAJE NATURAL"
        )),
        Semestre(6, listOf(
            "COMPUTO PARALELO",
            "REDES NEURONALES Y APRENDIZAJE PROFUNDO",
            "INGENIERIA DE SOFTWARE PARA SISTEMAS INTELIGENTES",
            "METODOLOGIA DE LA INVESTIGACION Y DIVULGACION CIENTIFICA"
        )),
        Semestre(7, listOf(
            "RECONOCIMIENTO DE VOZ",
            "TRABAJO TERMINAL I",
            "FORMULACION Y EVALUACION DE PROYECTOS INFORMATICOS"
        )),
        Semestre(8, listOf(
            "GESTION EMPRESARIAL",
            "TRABAJO TERMINAL II",
            "ESTANCIA PROFESIONAL",
            "DESARROLLO DE HABILIDADES SOCIALES PARA LA ALTA DIRECCION"
        ))
    )

    val IIA_OPTATIVAS: List<String> = listOf(
        "INNOVACION Y EMPRENDIMIENTO TECNOLOGICO",
        "TEMAS SELECTOS DE INTELIGENCIA ARTIFICIAL",
        "PROPIEDAD INTELECTUAL",
        "COMPUTO EN LA NUBE",
        "APLICACIONES DE LENGUAJE NATURAL",
        "TECNICAS DE PROGRAMACION PARA ROBOTS MOVILES",
        "SISTEMAS MULTIAGENTES",
        "INTERACCION HUMANO-MAQUINA",
        "APLICACIONES DE SISTEMAS MULTIAGENTES",
        "PROGRAMACION DE DISPOSITIVOS MOVILES",
        "MINERIA DE DATOS",
        "APLICACIONES DE INTELIGENCIA ARTIFICIAL EN SISTEMAS EMBEBIDOS",
        "BIG DATA",
        "TOPICOS SELECTOS DE ALGORITMOS BIOINSPIRADOS"
    )

    // ─── LCD ─────────────────────────────────────────────────────

    val LCD: List<Semestre> = listOf(
        Semestre(1, listOf(
            "FUNDAMENTOS DE PROGRAMACION",
            "MATEMATICAS DISCRETAS",
            "CALCULO",
            "COMUNICACION ORAL Y ESCRITA",
            "INTRODUCCION A LA CIENCIA DE DATOS"
        )),
        Semestre(2, listOf(
            "ALGORITMOS Y ESTRUCTURAS DE DATOS",
            "ALGEBRA LINEAL",
            "CALCULO MULTIVARIABLE",
            "ETICA Y LEGALIDAD",
            "FUNDAMENTOS ECONOMICOS"
        )),
        Semestre(3, listOf(
            "ANALISIS Y DISEÑO DE ALGORITMOS",
            "PROGRAMACION PARA CIENCIA DE DATOS",
            "PROBABILIDAD",
            "BASES DE DATOS",
            "METODOS NUMERICOS",
            "FINANZAS EMPRESARIALES"
        )),
        Semestre(4, listOf(
            "DESARROLLO DE APLICACIONES WEB",
            "COMPUTO DE ALTO DESEMPEÑO",
            "ESTADISTICA",
            "BASE DE DATOS AVANZADAS",
            "DESARROLLO DE APLICACIONES PARA ANALISIS DE DATOS",
            "LIDERAZGO PERSONAL"
        )),
        Semestre(5, listOf(
            "MINERIA DE DATOS",
            "MATEMATICAS AVANZADAS PARA CIENCIA DE DATOS",
            "PROCESOS ESTOCASTICOS",
            "APRENDIZAJE DE MAQUINA E INTELIGENCIA ARTIFICIAL",
            "ANALITICA Y VISUALIZACION DE DATOS",
            "METODOLOGIA DE LA INVESTIGACION Y DIVULGACION CIENTIFICA"
        )),
        Semestre(6, listOf(
            "MODELADO PREDICTIVO",
            "PROCESAMIENTO DE LENGUAJE NATURAL",
            "ANALISIS DE SERIES DE TIEMPO",
            "ANALITICA AVANZADA DE DATOS"
        )),
        Semestre(7, listOf(
            "BIG DATA",
            "MODELOS ECONOMETRICOS",
            "TRABAJO TERMINAL I",
            "ADMINISTRACION DE PROYECTOS DE TI"
        )),
        Semestre(8, listOf(
            "DESARROLLO DE HABILIDADES SOCIALES PARA LA ALTA DIRECCION",
            "GESTION EMPRESARIAL",
            "TRABAJO TERMINAL II",
            "ESTANCIA PROFESIONAL"
        ))
    )

    val LCD_OPTATIVAS: List<String> = listOf(
        "ESTADISTICA AVANZADA",
        "CIBERSEGURIDAD",
        "TEMAS SELECTOS DE INTELIGENCIA ARTIFICIAL",
        "PROTECCION DE DATOS",
        "TEMAS SELECTOS DE APRENDIZAJE PROFUNDO",
        "INNOVACION Y EMPRENDIMIENTO TECNOLOGICO",
        "TEMAS SELECTOS DE PROCESAMIENTO DE LENGUAJE NATURAL",
        "PROPIEDAD INTELECTUAL",
        "BIOINFORMATICA BASICA",
        "SIMULACION BASICA",
        "BIOINFORMATICA AVANZADA",
        "SIMULACION AVANZADA",
        "SISTEMAS DE INFORMACION GEOGRAFICA"
    )

    /** Todas las materias conocidas de ISC (semestres + optativas) */
    val ISC_TODAS: Set<String> by lazy {
        (ISC.flatMap { it.materias } + ISC_OPTATIVAS).toSet()
    }
}
