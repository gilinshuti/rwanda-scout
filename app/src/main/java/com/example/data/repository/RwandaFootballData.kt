package com.example.data.repository

import com.example.data.model.LineupEntity

object RwandaFootballData {
    val TEAMS = listOf(
        "APR FC",
        "Rayon Sports FC",
        "Police FC",
        "AS Kigali",
        "SC Kiyovu",
        "Mukura Victory Sports",
        "Musanze FC",
        "Gorilla FC",
        "Gasogi United",
        "Marines FC",
        "Bugesera FC",
        "Etincelles FC",
        "Rutsiro FC",
        "Amagaju FC",
        "Muhazi United",
        "Vision FC"
    )

    val COMPETITIONS = listOf(
        "Rwanda Premier League",
        "Peace Cup (Igikombe cy'Amahoro)",
        "Rwanda Super Cup",
        "Heroes Cup",
        "CAF Champions League",
        "CAF Confederation Cup",
        "Friendly Match"
    )

    val VENUES = listOf(
        "Amahoro National Stadium (Kigali)",
        "Kigali Pelé Stadium (Nyamirambo)",
        "Huye Stadium (Huye)",
        "Umuganda Stadium (Rubavu)",
        "Bugesera Stadium (Nyamata)",
        "Ubworoherane Stadium (Musanze)",
        "Ngoma Stadium (Kibungo)",
        "Mumena Stadium (Nyamirambo)"
    )

    // Pre-made realistic squads for rapid team roster auto-population
    fun generatePresetLineup(matchId: Long, teamSide: String, teamName: String): List<LineupEntity> {
        val isHome = teamSide.equals("HOME", ignoreCase = true)
        val playerRoster = when {
            teamName.contains("APR", ignoreCase = true) -> listOf(
                Pair(1, "Pavelh Ndzila") to "GK",
                Pair(2, "Fitina Omborenga") to "DEF",
                Pair(3, "Christian Ishimwe") to "DEF",
                Pair(4, "Prince Buregeya") to "DEF",
                Pair(5, "Salomon Bindjeme") to "DEF",
                Pair(6, "Taddeo Lwanga") to "MID",
                Pair(8, "Ruboneka Jean Bosco") to "MID",
                Pair(10, "Shaiboub Eldin") to "MID",
                Pair(7, "Ramadhan Niyibizi") to "FWD",
                Pair(9, "Victor Mbaoma") to "FWD",
                Pair(11, "Gilbert Mugisha") to "FWD",
                // Bench
                Pair(18, "Pierre Ishimwe") to "GK",
                Pair(14, "Yunusu Nshimiyimana") to "DEF",
                Pair(17, "Alain Kwitonda") to "MID",
                Pair(20, "Apam Bemol") to "FWD"
            )
            teamName.contains("Rayon", ignoreCase = true) -> listOf(
                Pair(1, "Khadime Ndiaye") to "GK",
                Pair(2, "Safi Serumogo") to "DEF",
                Pair(3, "Eric Ngendahimana") to "DEF",
                Pair(4, "Abdul Rwatubyaye") to "DEF",
                Pair(5, "Bugingo Hakim") to "DEF",
                Pair(6, "Aruna Madjaliwa") to "MID",
                Pair(8, "Kanamugire Roger") to "MID",
                Pair(10, "Muhire Kevin") to "MID",
                Pair(7, "Charles Bbaale") to "FWD",
                Pair(9, "Fallou Gningue") to "FWD",
                Pair(11, "Joackiam Ojera") to "FWD",
                // Bench
                Pair(16, "Simon Tamale") to "GK",
                Pair(13, "Mitima Isaac") to "DEF",
                Pair(15, "Youssef Rharb") to "MID",
                Pair(19, "Arsène Tuyisenge") to "FWD"
            )
            isHome -> listOf(
                Pair(1, "Olivier Kwizera") to "GK",
                Pair(2, "Michel Rusheshangoga") to "DEF",
                Pair(3, "Emmanuel Imanishimwe") to "DEF",
                Pair(4, "Thierry Manzi") to "DEF",
                Pair(5, "Ange Mutsinzi") to "DEF",
                Pair(6, "Djihad Bizimana") to "MID",
                Pair(8, "Ally Niyonzima") to "MID",
                Pair(10, "Haruna Niyonzima") to "MID",
                Pair(7, "Savio Nshuti") to "FWD",
                Pair(9, "Meddie Kagere") to "FWD",
                Pair(11, "Jacques Tuyisenge") to "FWD",
                // Bench
                Pair(12, "Fiacre Ntwari") to "GK",
                Pair(14, "Emery Bayisenge") to "DEF",
                Pair(15, "Yannick Mukunzi") to "MID",
                Pair(17, "Muhadjiri Hakizimana") to "FWD"
            )
            else -> listOf(
                Pair(1, "Eric Ndayishimiye") to "GK",
                Pair(2, "Faustin Usengimana") to "DEF",
                Pair(3, "Eric Rutanga") to "DEF",
                Pair(4, "Aimable Nsabimana") to "DEF",
                Pair(5, "Herve Rugwiro") to "DEF",
                Pair(6, "Fabrice Mugheni") to "MID",
                Pair(8, "Eric Nsabimana") to "MID",
                Pair(10, "Olivier Niyonzima") to "MID",
                Pair(7, "Dominique Savio") to "FWD",
                Pair(9, "Danny Usengimana") to "FWD",
                Pair(11, "Lague Byiringiro") to "FWD",
                // Bench
                Pair(22, "Maxime Wenssens") to "GK",
                Pair(13, "Latif Bishira") to "DEF",
                Pair(16, "Blaise Nishimwe") to "MID",
                Pair(18, "Innocent Nshuti") to "FWD"
            )
        }

        return playerRoster.mapIndexed { index, (playerInfo, pos) ->
            val isStarter = index < 11
            val isCaptain = index == 3 // Defender captain
            LineupEntity(
                matchId = matchId,
                team = teamSide,
                playerName = playerInfo.second,
                jerseyNumber = playerInfo.first,
                position = pos,
                isCaptain = isCaptain,
                isStarter = isStarter
            )
        }
    }
}
