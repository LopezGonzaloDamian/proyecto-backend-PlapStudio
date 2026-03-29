package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.repository.*
import java.time.LocalDate



object Bootstrap {

    fun cargarInfo(
        usuarios: RepositorioUsuarios,
        libros: RepositorioLibros,
        reservas: RepositorioReservas,
        resenias: RepositorioResenias,
    ) {

        // ============================================================
        // USUARIOS
        // ============================================================

        val gonza = Usuario(
            nombre = "Gonzalo", apellido = "Lopez",
            email = "gonza@gmail.com", username = "gonzaBookings",
            avatar = "https://randomuser.me/api/portraits/men/15.jpg",
            celular = "1199887766",
            ciudad = "Buenos Aires",
            descripcion = "Usuario de prueba para Mis Préstamos",
            bibliokarmas = 1500,
            fechaDeAlta = LocalDate.of(2024, 8, 1),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector_publicador
        )

        val ana = Usuario(
            nombre = "Ana", apellido = "García",
            email = "ana@mail.com", username = "anita",
            avatar = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic-cse.canva.com%2Fblob%2F1760022%2F1600w-vkBvE1d_xYA.jpg&f=1&nofb=1&ipt=9bc786c4345fb4b5012539347e8c4a1c3c1789fd8faa3a8477f347c66de40408",
            celular = "1122334455",
            ciudad = "Buenos Aires",
            descripcion = "Lectora ávida de clásicos",
            bibliokarmas = 800,
            fechaDeAlta = LocalDate.of(2023, 3, 10),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector
        )

        val pedro = Usuario(
            nombre = "Pedro", apellido = "bello",
            email = "pedro@mail.com", username = "pedrito",
            avatar = "https://randomuser.me/api/portraits/men/1.jpg",
            celular = "1122337755",
            ciudad = "Buenos Aires",
            descripcion = "Lector ávida de clásicos",
            bibliokarmas = 800,
            fechaDeAlta = LocalDate.of(2023, 3, 15),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector
        )

        val enzo = Usuario(
            nombre = "Enzo", apellido = "Francescoli",
            email = "enzo@mail.com", username = "enzito",
            avatar = "https://randomuser.me/api/portraits/men/2.jpg",
            celular = "1122334455",
            ciudad = "Buenos Aires",
            descripcion = "Lector de ficción",
            bibliokarmas = 800,
            fechaDeAlta = LocalDate.of(2023, 3, 19),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector
        )

        val marcos = Usuario(
            nombre = "Marcos", apellido = "Pérez",
            email = "marcos@mail.com", username = "marquitos99",
            avatar = "https://randomuser.me/api/portraits/men/3.jpg",
            celular = "1144556677",
            ciudad = "Rosario",
            descripcion = "Fan de la ciencia ficción",
            bibliokarmas = 1200,
            fechaDeAlta = LocalDate.of(2022, 7, 15),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector
        )

        val lucia = Usuario(
            nombre = "Lucía", apellido = "Fernández",
            email = "lucia@mail.com", username = "luciaf",
            avatar = "https://randomuser.me/api/portraits/women/1.jpg",
            celular = "1166778899",
            ciudad = "Córdoba",
            descripcion = "Coleccionista y lectora",
            bibliokarmas = 450,
            fechaDeAlta = LocalDate.of(2024, 1, 5),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector_publicador
        )

        val diego = Usuario(
            nombre = "Diego", apellido = "Romero",
            email = "diego@mail.com", username = "dieguitoR",
            avatar = "https://randomuser.me/api/portraits/men/4.jpg",
            celular = "1188990011",
            ciudad = "Mendoza",
            descripcion = "Publica y lee drama",
            bibliokarmas = 2300,
            fechaDeAlta = LocalDate.of(2021, 11, 20),
            password = "1234",
            tipoUsuario = TipoDeUsuario.lector_publicador
        )

        usuarios.create(ana)
        usuarios.create(marcos)
        usuarios.create(lucia)
        usuarios.create(diego)
        usuarios.create(enzo)
        usuarios.create(pedro)
        usuarios.create(gonza)

        // ============================================================
        // LIBROS
        // ============================================================
        val libroGonza1 = LibroComun(
            titulo = "1984",
            descripcion = "Distopía clásica de George Orwell",
            genero = Genero.ciencia_ficcion,
            autor = "George Orwell",
            cantidadDePaginas = 328,
            isbn13 = "978-0452284234",
            idioma = Idioma.ingles,
            editorial = "Signet Classics",
            fechaDePublicacion = LocalDate.of(1949, 6, 8),
            estado = Estado.muy_bueno,
            duenio = gonza,
            imagen = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1657781256i/61439040.jpg"
        )

        val libroGonza2 =  LibroDedicado(
            titulo = "To Kill a Mockingbird",
            descripcion = "Clásico sobre justicia y prejuicio",
            genero = Genero.literatura_clasica,
            autor = "Harper Lee",
            cantidadDePaginas = 281,
            isbn13 = "978-0061120084",
            idioma = Idioma.ingles,
            editorial = "Harper Perennial",
            fechaDePublicacion = LocalDate.of(1960, 7, 11),
            estado = Estado.excelente,
            duenio = gonza,
            imagen = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1553383690i/2657.jpg"
        )

        val libroGonza3 = LibroColeccionable(
            titulo = "The Catcher in the Rye",
            descripcion = "Holden Caulfield y su mirada del mundo",
            genero = Genero.literatura_clasica,
            autor = "J.D. Salinger",
            cantidadDePaginas = 214,
            isbn13 = "978-0316769480",
            idioma = Idioma.ingles,
            editorial = "Little, Brown and Company",
            fechaDePublicacion = LocalDate.of(1951, 7, 16),
            estado = Estado.bueno,
            duenio = gonza,
            imagen = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1398034300i/5107.jpg"
        )

        val elAleph = LibroComun(
            titulo = "El Aleph",
            descripcion = "Cuentos fantásticos de Borges",
            genero = Genero.literatura_clasica,
            autor = "Jorge Luis Borges",
            cantidadDePaginas = 224,
            isbn13 = "978-0060935286",
            idioma = Idioma.espaniol,
            editorial = "Emecé",
            fechaDePublicacion = LocalDate.of(1949, 1, 1),
            estado = Estado.bueno,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%2Fid%2FOIP.bJE2g7_HVFBc403Q7CiF7QHaLT%3Fpid%3DApi&f=1&ipt=09cd3386a3619b120400097da05fe7c124dbc34a4af00eb719ee30cc4c178506&ipo=images"
        )

        val cienAniosDeSoledad = LibroComun(
            titulo = "Cien años de soledad",
            descripcion = "Saga de los Buendía",
            genero = Genero.drama,
            autor = "Gabriel García Márquez",
            cantidadDePaginas = 471,
            isbn13 = "978-0060883287",
            idioma = Idioma.espaniol,
            editorial = "Sudamericana",
            fechaDePublicacion = LocalDate.of(1967, 5, 30),
            estado = Estado.muy_bueno,
            duenio = marcos,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.k4wW1YD2XUZt5E2fLsmHMgHaJ-%3Fpid%3DApi&f=1&ipt=d26615694c9113815ea935e1f836b3bfa61e76b23bf0c18c46e32c1e1bdbaa83&ipo=images"
        )

        val donQuijote = LibroComun(
            titulo = "Don Quijote de la Mancha",
            descripcion = "El ingenioso hidalgo",
            genero = Genero.literatura_clasica,
            autor = "Miguel de Cervantes",
            cantidadDePaginas = 863,
            isbn13 = "978-8437604947",
            idioma = Idioma.espaniol,
            editorial = "Cátedra",
            fechaDePublicacion = LocalDate.of(1605, 1, 16),
            estado = Estado.regular,
            duenio = lucia,
            imagen = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.polifemo.com%2Fstatic%2Fimg%2Fportadas%2F_visd_0000JPG01EME.jpg&f=1&nofb=1&ipt=1e9a24243c853967f9350d6b9cac6f6fd5a3b8e540d75388cd3d4015b06bc9e8"
        )

        val laMetamorfosis = LibroComun(
            titulo = "La metamorfosis",
            descripcion = "Gregor Samsa despierta convertido en insecto",
            genero = Genero.drama,
            autor = "Franz Kafka",
            cantidadDePaginas = 96,
            isbn13 = "978-8420689878",
            idioma = Idioma.espaniol,
            editorial = "Alianza",
            fechaDePublicacion = LocalDate.of(1915, 10, 15),
            estado = Estado.excelente,
            duenio = diego,
            imagen = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%2Fid%2FOIP.RSCpbSl4vIJxO74QrSfLRwHaLM%3Fpid%3DApi&f=1&ipt=5e0588206534c763eb89c55c3be10e00999aa7f39d8b67f1a12a5ae0533828b0"
        )

        val crimen = LibroComun(
            titulo = "Crimen y castigo",
            descripcion = "Raskolnikov y su dilema moral",
            genero = Genero.drama,
            autor = "Fiódor Dostoievski",
            cantidadDePaginas = 671,
            isbn13 = "978-8420654531",
            idioma = Idioma.espaniol,
            editorial = "Alianza",
            fechaDePublicacion = LocalDate.of(1866, 1, 1),
            estado = Estado.bueno,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%2Fid%2FOIP.V9OyndtvlsmlPzTNgcJUywHaKS%3Fpid%3DApi&f=1&ipt=3d2b5520daa07e7b39c7810f5244d281e6bd5d226fcb05bd4f865fd653c0425c&ipo=images"
        )

        val elPrincipito = LibroComun(
            titulo = "El Principito",
            descripcion = "Un aviador conoce a un pequeño príncipe",
            genero = Genero.literatura_clasica,
            autor = "Antoine de Saint-Exupéry",
            cantidadDePaginas = 96,
            isbn13 = "978-8426132026",
            idioma = Idioma.frances,
            editorial = "Salamandra",
            fechaDePublicacion = LocalDate.of(1943, 4, 6),
            estado = Estado.excelente,
            duenio = marcos,
            imagen = "/img/elprincipito.jpg"
        )

        val orgullo = LibroComun(
            titulo = "Orgullo y prejuicio",
            descripcion = "Elizabeth Bennet y Mr. Darcy",
            genero = Genero.romance,
            autor = "Jane Austen",
            cantidadDePaginas = 432,
            isbn13 = "978-0141439518",
            idioma = Idioma.ingles,
            editorial = "Penguin",
            fechaDePublicacion = LocalDate.of(1813, 1, 28),
            estado = Estado.muy_bueno,
            duenio = lucia,
            imagen = ""
        )

        val elHobbit = LibroComun(
            titulo = "El Hobbit",
            descripcion = "La aventura de Bilbo Bolsón",
            genero = Genero.ciencia_ficcion,
            autor = "J.R.R. Tolkien",
            cantidadDePaginas = 310,
            isbn13 = "978-8445071793",
            idioma = Idioma.espaniol,
            editorial = "Minotauro",
            fechaDePublicacion = LocalDate.of(1937, 9, 21),
            estado = Estado.bueno,
            duenio = diego,
            imagen = "/img/books/el-hobbit.jpg"
        )

        val rayuela = LibroDedicado(
            titulo = "Rayuela",
            descripcion = "Novela experimental de Cortázar — dedicada a los lectores cómplices",
            genero = Genero.literatura_clasica,
            autor = "Julio Cortázar",
            cantidadDePaginas = 600,
            isbn13 = "978-8420483204",
            idioma = Idioma.espaniol,
            editorial = "Sudamericana",
            fechaDePublicacion = LocalDate.of(1963, 6, 28),
            estado = Estado.muy_bueno,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%2Fid%2FOIP.Yu51gXhb5rpsx8Npu_ptAQAAAA%3Fpid%3DApi&f=1&ipt=45b67045320d54b38b8703e418c43170f20be70780776c3d53d3ab1843e2fd20&ipo=images"
        )

        val laHojarasca = LibroDedicado(
            titulo = "La hojarasca",
            descripcion = "Primera novela de García Márquez — dedicada a su madre",
            genero = Genero.drama,
            autor = "Gabriel García Márquez",
            cantidadDePaginas = 144,
            isbn13 = "978-8439701654",
            idioma = Idioma.espaniol,
            editorial = "Sudamericana",
            fechaDePublicacion = LocalDate.of(1955, 1, 1),
            estado = Estado.bueno,
            duenio = marcos,
            imagen = "/img/books/la-hojarrasca.jpg"
        )

        val cartaAMiHija = LibroDedicado(
            titulo = "Carta a mi hija",
            descripcion = "Maya Angelou — con dedicatoria personal",
            genero = Genero.autoayuda,
            autor = "Maya Angelou",
            cantidadDePaginas = 96,
            isbn13 = "978-0812979744",
            idioma = Idioma.ingles,
            editorial = "Random House",
            fechaDePublicacion = LocalDate.of(1998, 3, 1),
            estado = Estado.excelente,
            duenio = lucia,
            imagen = "/img/books/carta-a-mi-hija.jpg"
        )

        val laInsoportable = LibroDedicado(
            titulo = "La insoportable levedad del ser",
            descripcion = "Dedicada a la memoria de Milan Kundera",
            genero = Genero.romance,
            autor = "Milan Kundera",
            cantidadDePaginas = 368,
            isbn13 = "978-8072604058",
            idioma = Idioma.espaniol,
            editorial = "Tusquets",
            fechaDePublicacion = LocalDate.of(1984, 1, 1),
            estado = Estado.muy_bueno,
            duenio = diego,
            imagen = ""
        )

        val elamorEnTiempos = LibroDedicado(
            titulo = "El amor en los tiempos del cólera",
            descripcion = "Dedicada a Carmen Balcells",
            genero = Genero.romance,
            autor = "Gabriel García Márquez",
            cantidadDePaginas = 348,
            isbn13 = "978-8439702002",
            idioma = Idioma.espaniol,
            editorial = "Oveja Negra",
            fechaDePublicacion = LocalDate.of(1985, 12, 5),
            estado = Estado.bueno,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.explicit.bing.net%2Fth%2Fid%2FOIP.ED0rPZaTjA82QPUP4PzybgAAAA%3Fpid%3DApi&f=1&ipt=64302121631f45fed61b2a264a0d161077c315d2e4f63a1547b00672afeb5eb8&ipo=images"
        )

        val ficciones = LibroDedicado(
            titulo = "Ficciones",
            descripcion = "Borges — con dedicatoria manuscrita",
            genero = Genero.literatura_clasica,
            autor = "Jorge Luis Borges",
            cantidadDePaginas = 174,
            isbn13 = "978-8420483143",
            idioma = Idioma.espaniol,
            editorial = "Emecé",
            fechaDePublicacion = LocalDate.of(1944, 1, 1),
            estado = Estado.regular,
            duenio = marcos,
            imagen = "/img/books/ficciones.jpg"
        )

        val madameBovary = LibroDedicado(
            titulo = "Madame Bovary",
            descripcion = "Dedicada a Marie-Anne Flaubert",
            genero = Genero.romance,
            autor = "Gustave Flaubert",
            cantidadDePaginas = 392,
            isbn13 = "978-0140449127",
            idioma = Idioma.frances,
            editorial = "Penguin",
            fechaDePublicacion = LocalDate.of(1857, 4, 15),
            estado = Estado.bueno,
            duenio = lucia,
            imagen = ""
        )

        val sobreElAmor = LibroDedicado(
            titulo = "Sobre el amor y la soledad",
            descripcion = "Krishnamurti — ejemplar dedicado",
            genero = Genero.autoayuda,
            autor = "Jiddu Krishnamurti",
            cantidadDePaginas = 192,
            isbn13 = "978-8472453502",
            idioma = Idioma.espaniol,
            editorial = "Edaf",
            fechaDePublicacion = LocalDate.of(1992, 1, 1),
            estado = Estado.excelente,
            duenio = diego,
            imagen = ""
        )

        val ulises = LibroColeccionable(
            titulo = "Ulises",
            descripcion = "Edición de colección numerada — Joyce",
            genero = Genero.literatura_clasica,
            autor = "James Joyce",
            cantidadDePaginas = 1000,
            isbn13 = "978-0199543190",
            idioma = Idioma.ingles,
            editorial = "Oxford University Press",
            fechaDePublicacion = LocalDate.of(1922, 2, 2),
            estado = Estado.excelente,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.ikDxmUwic8n1y2e_WFCAkgAAAA%3Fpid%3DApi&f=1&ipt=4300609f0fe8193624f96471dd71306c3e8a1c26fb4aad946605980602458a1a&ipo=images"
        )

        val elNombre = LibroColeccionable(
            titulo = "El nombre de la rosa",
            descripcion = "Edición ilustrada de colección",
            genero = Genero.drama,
            autor = "Umberto Eco",
            cantidadDePaginas = 502,
            isbn13 = "978-8435014981",
            idioma = Idioma.espaniol,
            editorial = "Lumen",
            fechaDePublicacion = LocalDate.of(1980, 1, 1),
            estado = Estado.excelente,
            duenio = marcos,
            imagen = ""
        )

        val fahrenheit = LibroColeccionable(
            titulo = "Fahrenheit 451",
            descripcion = "Edición 70° aniversario",
            genero = Genero.ciencia_ficcion,
            autor = "Ray Bradbury",
            cantidadDePaginas = 256,
            isbn13 = "978-1451673319",
            idioma = Idioma.ingles,
            editorial = "Simon & Schuster",
            fechaDePublicacion = LocalDate.of(1953, 10, 19),
            estado = Estado.excelente,
            duenio = lucia,
            imagen = ""
        )

        val neuromancer = LibroColeccionable(
            titulo = "Neuromancer",
            descripcion = "Edición especial numerada de colección",
            genero = Genero.ciencia_ficcion,
            autor = "William Gibson",
            cantidadDePaginas = 271,
            isbn13 = "978-0441569564",
            idioma = Idioma.ingles,
            editorial = "Ace Books",
            fechaDePublicacion = LocalDate.of(1984, 7, 1),
            estado = Estado.excelente,
            duenio = diego,
            imagen = ""
        )

        val fundacion = LibroColeccionable(
            titulo = "Fundación",
            descripcion = "Trilogía original — edición de colección Asimov",
            genero = Genero.ciencia_ficcion,
            autor = "Isaac Asimov",
            cantidadDePaginas = 244,
            isbn13 = "978-8445070642",
            idioma = Idioma.espaniol,
            editorial = "Minotauro",
            fechaDePublicacion = LocalDate.of(1951, 5, 1),
            estado = Estado.excelente,
            duenio = ana,
            imagen = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.Eq-Q-4oNy3e4zSxEwxO2DAHaLQ%3Fpid%3DApi&f=1&ipt=096953e6d29e1db228a8dc3d6873627bd320c43a9a8fe1bcecc3069cc7e85821&ipo=images"
        )

        val dune = LibroColeccionable(
            titulo = "Dune",
            descripcion = "Edición ilustrada de lujo",
            genero = Genero.ciencia_ficcion,
            autor = "Frank Herbert",
            cantidadDePaginas = 412,
            isbn13 = "978-0593310324",
            idioma = Idioma.ingles,
            editorial = "Ace Books",
            fechaDePublicacion = LocalDate.of(1965, 8, 1),
            estado = Estado.excelente,
            duenio = marcos,
            imagen = ""
        )

        val elDisenioDeLoComun = LibroColeccionable(
            titulo = "El diseño de lo cotidiano",
            descripcion = "Edición especial de colección — Norman",
            genero = Genero.disenio,
            autor = "Donald Norman",
            cantidadDePaginas = 368,
            isbn13 = "978-0465067107",
            idioma = Idioma.ingles,
            editorial = "Basic Books",
            fechaDePublicacion = LocalDate.of(1988, 1, 1),
            estado = Estado.muy_bueno,
            duenio = lucia,
            imagen = ""
        )

        val watchmen = LibroColeccionable(
            titulo = "Watchmen",
            descripcion = "Edición absoluta de colección",
            genero = Genero.ciencia_ficcion,
            autor = "Alan Moore",
            cantidadDePaginas = 416,
            isbn13 = "978-1401222667",
            idioma = Idioma.ingles,
            editorial = "DC Comics",
            fechaDePublicacion = LocalDate.of(1987, 9, 1),
            estado = Estado.excelente,
            duenio = diego,
            imagen = ""
        )

        listOf(
            libroGonza1, libroGonza2, libroGonza3,
            elAleph, cienAniosDeSoledad, donQuijote, laMetamorfosis,
            crimen, elPrincipito, orgullo, elHobbit,
            rayuela, laHojarasca, cartaAMiHija, laInsoportable,
            elamorEnTiempos, ficciones, madameBovary, sobreElAmor,
            ulises, elNombre, fahrenheit, neuromancer,
            fundacion, dune, elDisenioDeLoComun, watchmen
        ).forEach { libros.create(it) }

        // ============================================================
        // RESERVAS
        // ============================================================
        

        val reservaAna1 = ana.reservar(
            elAleph,
            desde = LocalDate.of(2024, 1, 1),
            hasta = LocalDate.of(2024, 1, 20)
        )

        val reservaAna2 = ana.reservar(
            ulises,
            desde = LocalDate.of(2024, 4, 10),
            hasta = LocalDate.of(2024, 5, 1)
        )

        val reservaAna3 = ana.reservar(
            rayuela,
            desde = LocalDate.now().plusDays(3),
            hasta = LocalDate.now().plusDays(20)
        )

        val reservaEnzo1 = enzo.reservar(
            elAleph,
            desde = LocalDate.of(2025, 1, 1),
            hasta = LocalDate.of(2025, 1, 20)
        )

        val reservaPedro1 = pedro.reservar(
            elAleph,
            desde = LocalDate.of(2022, 1, 1),
            hasta = LocalDate.of(2022, 1, 20)
        )

        val reservaMarcos1 = marcos.reservar(
            fahrenheit,
            desde = LocalDate.of(2024, 1, 5),
            hasta = LocalDate.of(2024, 1, 25)
        )

        val reservaMarcos2 = marcos.reservar(
            cienAniosDeSoledad,
            desde = LocalDate.of(2024, 3, 1),
            hasta = LocalDate.of(2024, 3, 20)
        )

        val reservaMarcos3 = marcos.reservar(
            dune,
            desde = LocalDate.now().plusDays(1),
            hasta = LocalDate.now().plusDays(14)
        )

        val reservaMarcos4 = marcos.reservar(
            elAleph,
            desde = LocalDate.of(2025, 10, 10),
            hasta = LocalDate.now().plusDays(10)
        )

        val reservaLucia1 = lucia.reservar(
            elNombre,
            desde = LocalDate.of(2024, 5, 1),
            hasta = LocalDate.of(2024, 5, 20)
        )

        val reservaLucia2 = lucia.reservar(
            orgullo,
            desde = LocalDate.of(2024, 6, 15),
            hasta = LocalDate.of(2024, 7, 5)
        )

        val reservaDiego1 = diego.reservar(
            neuromancer,
            desde = LocalDate.of(2024, 2, 10),
            hasta = LocalDate.of(2024, 3, 1)
        )

        val reservaDiego2 = diego.reservar(
            laInsoportable,
            desde = LocalDate.of(2024, 7, 1),
            hasta = LocalDate.of(2024, 7, 20)
        )

        val reservaDiego3 = diego.reservar(
            watchmen,
            desde = LocalDate.now().plusDays(2),
            hasta = LocalDate.now().plusDays(10)
        )
        
        // ---------- PRESTADOS A GONZA (FOR_ME) ----------

        // Devuelto y ya reseñado
        val reservaGonza1 = gonza.reservar(
            laHojarasca,
            desde = LocalDate.of(2024, 1, 1),
            hasta = LocalDate.of(2024, 1, 20)
        )

        val reservaGonza10 = gonza.reservar(
            laHojarasca,
            desde = LocalDate.of(2025, 1, 1),
            hasta = LocalDate.of(2025, 1, 20)
        )

        // Devuelto y sin reseña -> canReview = true
        val reservaGonza2 = gonza.reservar(
            cartaAMiHija,
            desde = LocalDate.of(2024, 4, 10),
            hasta = LocalDate.of(2024, 5, 1)
        )

        // Activo
        val reservaGonza3 = gonza.reservar(
            ficciones,
            desde = LocalDate.now().minusDays(4),
            hasta = LocalDate.now().plusDays(6)
        )

        // Próximo a vencer
        val reservaGonza4 = gonza.reservar(
            elHobbit,
            desde = LocalDate.now().minusDays(8),
            hasta = LocalDate.now().plusDays(1)
        )

        // ---------- PRESTADOS POR GONZA (BY_ME) ----------

        // Devuelto
        val reservaMarcosGonza1 = marcos.reservar(
            libroGonza1,
            desde = LocalDate.of(2024, 2, 1),
            hasta = LocalDate.of(2024, 2, 20)
        )

        // Devuelto
        val reservaAnaGonza1 = ana.reservar(
            libroGonza2,
            desde = LocalDate.of(2024, 3, 10),
            hasta = LocalDate.of(2024, 3, 25)
        )

        // Activo
        val reservaLuciaGonza1 = lucia.reservar(
            libroGonza3,
            desde = LocalDate.now().minusDays(3),
            hasta = LocalDate.now().plusDays(7)
        )

        // Próximo a vencer
        val reservaDiegoGonza1 = diego.reservar(
            libroGonza1,
            desde = LocalDate.now().minusDays(9),
            hasta = LocalDate.now().plusDays(2)
        )

        // Devuelto extra para paginación
        val reservaPedroGonza1 = pedro.reservar(
            libroGonza2,
            desde = LocalDate.of(2023, 10, 1),
            hasta = LocalDate.of(2023, 10, 15)
        )

        listOf(
            reservaAna1, reservaAna2, reservaAna3,
            reservaEnzo1, reservaPedro1, reservaGonza10,
            reservaMarcos1, reservaMarcos2, reservaMarcos3, reservaMarcos4,
            reservaLucia1, reservaLucia2,
            reservaDiego1, reservaDiego2, reservaDiego3,
            reservaGonza1, reservaGonza2, reservaGonza3, reservaGonza4,
            reservaMarcosGonza1, reservaAnaGonza1, reservaLuciaGonza1,
            reservaDiegoGonza1, reservaPedroGonza1
        ).forEach { reservas.create(it) }

        // ============================================================
        // RESEÑAS
        // ============================================================

        reservaAna1.reseniar(resenias, 5, "Una obra maestra absoluta, Borges en su máxima expresión.")
        reservaAna2.reseniar(resenias, 4, "Exigente como pocas, pero la recompensa es enorme.")
        reservaEnzo1.reseniar(resenias, 5, "Excelente libro, muy entretenido de principio a fin. La historia te atrapa y los personajes están muy bien desarrollados.")
        reservaPedro1.reseniar(resenias, 4, "Muy buena lectura, la narrativa es fluida y fácil de seguir. Ideal para desconectarse y disfrutar un buen libro.")

        reservaMarcos1.reseniar(resenias, 5, "Más vigente hoy que cuando fue escrita, imprescindible.")
        reservaMarcos2.reseniar(resenias, 4, "El realismo mágico elevado a su máxima potencia.")

        reservaLucia1.reseniar(resenias, 5, "Eco construye un thriller medieval con una erudición impresionante.")
        reservaLucia2.reseniar(resenias, 4, "Austen es una maestra de la ironía social.")

        reservaDiego1.reseniar(resenias, 5, "Gibson inventó el futuro, esta edición numerada es un objeto de culto.")
        reservaDiego2.reseniar(resenias, 4, "Kundera mezcla filosofía y ficción de una manera magistral.")

        // FOR_ME
        reservaGonza1.reseniar(resenias, 5, "Una obra maestra absoluta, Borges en su máxima expresión.")

        // BY_ME
        reservaMarcosGonza1.reseniar(resenias, 5, "Impactante de principio a fin, una lectura imprescindible.")
        reservaAnaGonza1.reseniar(resenias, 4, "Muy buena lectura, atrapante y muy bien escrita.")
        reservaPedroGonza1.reseniar(resenias, 4, "Un clásico que vale la pena revisitar.")

    }
}
