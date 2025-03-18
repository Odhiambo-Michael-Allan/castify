package com.squad.castify.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.squad.castify.core.model.Category
import com.squad.castify.core.model.DarkThemeConfig
import com.squad.castify.core.model.Episode
import com.squad.castify.core.model.FollowablePodcast
import com.squad.castify.core.model.Podcast
import com.squad.castify.core.model.ThemeBrand
import com.squad.castify.core.model.UserData
import com.squad.castify.core.model.UserEpisode
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class CategoryPodcastEpisodePreviewParameterProvider : PreviewParameterProvider<PreviewData> {
    override val values: Sequence<PreviewData> = sequenceOf(
        PreviewData(
            categories = PreviewParameterData.categories,
            podcasts = PreviewParameterData.podcasts,
            episodes = PreviewParameterData.episodes
        )
    )
}

data class PreviewData(
    val categories: List<Category>,
    val podcasts: List<FollowablePodcast>,
    val episodes: List<UserEpisode>
)

object PreviewParameterData {

    private val userData: UserData = UserData(
        themeBrand = ThemeBrand.ANDROID,
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        shouldHideOnboarding = false,
        followedPodcasts = emptySet(),
        listenedEpisodes = emptySet()
    )

    val categories = listOf(
        Category(
            id = "0",
            name = "Technology"
        ),
        Category(
            id = "1",
            name = "Science"
        ),
        Category(
            id = "4",
            name = "Society & Culture"
        ),
        Category(
            id = "11",
            name = "News"
        )
    )

    val podcasts = listOf(
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://feeds.libsyn.com/244409/rss",
                title = "Now in Android",
                description = "This show gives listeners a quick run-down on things that the Android team has done recently that developers may want to check out. It covers library and platform releases, articles, videos, podcasts, samples, codelabs - whatever seems relevant and interesting for Android developers.\n\nSubscribe to Android Developers YouTube → https://goo.gle/AndroidDevs\n\nAndroid’s a big platform and there are many things being released all the time; listen to this podcast to stay up to date on what those things are." ,
                author = "gdevs.podcast@gmail.com (gdevs.podcast@gmail.com)",
                imageUrl = "https://static.libsyn.com/p/assets/1/4/f/9/14f959f911553fcc27a2322813b393ee/NIA000_PodcastThumbnail-20241010-vj0kfz2md4.png",
                categories = listOf( categories[0], categories[1] )
            ),
            isFollowed = false
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://feeds.feedburner.com/blogspot/AndroidDevelopersBackstage",
                title = "Android Developers Backstage",
                description = "Android Backstage, a podcast by and for Android developers. Hosted by developers from the Android engineering team, this show covers topics of interest to Android programmers, with in-depth discussions and interviews with engineers on the Android team at Google.",
                author = "gdevs.podcast@gmail.com (gdevs.podcast@gmail.com)",
                imageUrl = "https://static.libsyn.com/p/assets/c/9/e/0/c9e07a90cf263f3b40be95ea3302a6a1/Android_Devs_Backstage_Thumb_v2.png",
                categories = emptyList()
            ),
            isFollowed = true
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://www.omnycontent.com/d/playlist/aaea4e69-af51-495e-afc9-a9760146922b/dc5b55ca-5f00-4063-b47f-ab870163d2b7/ca63aa52-ef7b-43ee-8ba5-ab8701645231/podcast.rss",
                title = "No Stupid Questions",
                description = "Research psychologist Angela Duckworth (author of \"Grit\") and tech and sports executive Mike Maughan really like to ask people questions, and they believe there’s no such thing as a stupid one. So they have a podcast where they can ask each other as many “stupid questions” as they want. New episodes each week. \"No Stupid Questions\" is a production of the Freakonomics Radio Network.\n\n\nTo get every show in our network without ads and a monthly bonus episode of Freakonomics Radio, sign up for SiriusXM Podcasts+ on Apple Podcasts at http://apple.co/SiriusXM.",
                imageUrl = "https://image.simplecastcdn.com/images/42d2a406-817e-4291-8e96-123ac167308f/d384aac1-850f-43d4-b45a-1c965fdaae21/3000x3000/nsq-3000x3000.jpg?aid\u003drss_feed",
                categories = emptyList()
            ),
            isFollowed = true
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://audioboom.com/channels/2399216.rss",
                title = "No Such Thing As A Fish",
                description = "Award-winning podcast from the QI offices in which the writers of the hit BBC show discuss the best things they\u0027ve found out this week.\n\nHosted by Dan Schreiber (@schreiberland) with James Harkin (@jamesharkin), Andrew Hunter Murray (@andrewhunterm), and Anna Ptaszynski (#GetAnnaOnTwitter)",
                imageUrl = "https://audioboom.com/i/41239450.png",
                categories = emptyList()
            ),
            isFollowed = false
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://feeds.megaphone.fm/replyall",
                title = "Reply All",
                description = "\"\u0027A podcast about the internet\u0027 that is actually an unfailingly original exploration of modern life and how to survive it.\" - The Guardian. Hosted by Alex Goldman and Emmanuel Dzotsi from Gimlet.",
                imageUrl = "https://megaphone.imgix.net/podcasts/05f71746-a825-11e5-aeb5-a7a572df575e/image/uploads_2F1610700524297-f3e2p6fbm79-8b4536488226b56e93a99ce9f7b555b5_2FReplyAll_ShowArt.png?ixlib\u003drails-4.3.1\u0026max-w\u003d3000\u0026max-h\u003d3000\u0026fit\u003dcrop\u0026auto\u003dformat,compress",
                categories = emptyList()
            ),
            isFollowed = false
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://feeds.thisamericanlife.org/talpodcast",
                title = "This American Life",
                imageUrl = "https://thisamericanlife.org/sites/all/themes/thislife/img/tal-logo-3000x3000.png",
                categories = emptyList()
            ),
            isFollowed = false
        ),
        FollowablePodcast(
            podcast = Podcast(
                uri = "https://feeds.npr.org/510289/podcast.xml",
                title = "Planet Money",
                imageUrl = "https://media.npr.org/assets/img/2022/10/24/pm_new_tile_2022_sq-b4af5aab11c84cfae38eafa1db74a6da943d4e7f.jpg?s\u003d1400\u0026c\u003d66\u0026f\u003djpg",
                categories = emptyList()
            ),
            isFollowed = false
        ),
    )

    val episodes = listOf(
        UserEpisode(
            episode = Episode(
                uri = "http://nowinandroid.libsyn.com/112-android-16-developer-preview-1-passkeys-spotlight-week-and-more",
                title = "112 - Android 16 Developer Preview 1, Passkeys Spotlight Week, and more!",
                subTitle = "Welcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover the First Developer Preview of Android 16, our Spotlight Week on Passkeys, Stability and Performance...",
                summary = "\u003cp\u003eWelcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover the First Developer Preview of Android 16, our Spotlight Week on Passkeys, Stability and Performance Improvements to the Android Emulator and more!\u003c/p\u003e \u003cp\u003eFor links to these items, check out Now in Android #112 on Medium → https://goo.gle/3OUlGMV \u003c/p\u003e \u003cp\u003eWatch more Now in Android → https://goo.gle/now-in-android \u003cbr /\u003e Subscribe to Android Developers YouTube → https://goo.gle/AndroidDevs \u003c/p\u003e",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[1].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "http://nowinandroid.libsyn.com/111-android-api-level-and-schedule-updates-gemini-in-android-studio-google-play",
                title = "111 - Android API level and schedule updates, Gemini in Android Studio, Google Play",
                subTitle = "Welcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover Android API level and schedule updates, Gemini in Android Studio, Google Play, Spotlight weeks on Adaptive...",
                summary = "\u003cp\u003eWelcome to Now in Android, your ongoing guide to what\u0027s new and notable in the world of Android development. In this episode, we’ll cover Android API level and schedule updates, Gemini in Android Studio, Google Play, Spotlight weeks on Adaptive Android Apps and Android AI, and more!\u003c/p\u003e \u003cp\u003eFor links to these items, check out Now in Android #111 on \u003ca href\u003d \"https://medium.com/@dagalpin/9c60a12a8473\"\u003eMedium\u003c/a\u003e \u003c/p\u003e \u003cp\u003eWatch \u003ca href\u003d \"https://www.youtube.com/playlist?list\u003dPLWz5rJ2EKKc9AtgKMBBdphI-mrx8XzW56\"\u003eNow in Android\u003c/a\u003e videos\u003cbr /\u003e Subscribe to \u003ca href\u003d \"https://www.youtube.com/channel/UCVHFbqXqoYvEWM1Ddxl0QDg\"\u003eAndroid Developers YouTube\u003c/a\u003e\u003c/p\u003e",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[1].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "http://adbackstage.libsyn.com/episode-211-rules-about-performance-tools",
                title = "Episode 211: Rules about performance tools",
                subTitle = "In this episode Chet, Romain and Tor chat with Shai Barack about how the Android platform team studies performance and understands system health - and what is system health anyway? We talk about measuring performance, deciding trade-offs, and our...",
                summary = "In this episode Chet, Romain and Tor chat with Shai Barack about how the Android platform team studies performance and understands system health - and what is system health anyway? We talk about measuring performance, deciding trade-offs, and our favorite tools such as Perfetto, Compiler Explorer, and Android Studio\u0027s Memory Profiler.   Chapters: Intro (00:00) System health (0:27) Efforts to make apps more efficient (3:35) Telemetry data (5:59) Trade offs between long battery life and good performance (8:21) Scheduling groups (10:38) Static drain (13:32) Collaborating with App developers vs operating system (19:10) High refresh rates (23:26) Reach vs engagement (32:02) What tools does your team use to optimize performance? (34:10) Godbolt.org (37:09) Demystifying (39:39) The best tools are multi-player (43:52) R8 or R-Not? (45:42) Optimizing for feature sets (48:05) Tools, not Rules (50:08) What are the tools I should be aware of as an app developer looking to upscale performance? (54:36) Allocation tracker (55:37) Open source tools (57:08) Useful resources for devs to understand various tools (59:04) Final thoughts (1:06:19) Links: Compiler Explorer → https://goo.gle/3Zbq6DV  Perfetto → https://goo.gle/3OtD3UK and https://goo.gle/3B3S3p5  Tools, not Rules  → https://goo.gle/416CyY7    Shai:  Romain: @romainguy, threads.net/@romainguy, romainguy@androiddev.social Tor: threads.net/@tor.norbye and tornorbye@androiddev.social Chet: @chethaase, threads.net/@chet.haase, and chethaase@androiddev.social   Catch videos on YouTube → https://goo.gle/adb-podcast   Subscribe to Android Developers  → https://goo.gle/AndroidDevs",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[2].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "http://adbackstage.libsyn.com/episode-202-androidx-gradle-and-metalava",
                title = "Episode 202: AndroidX, Gradle and Metalava",
                subTitle = "In this episode, Tor and Romain chat with Aurimas Liutikas from the AndroidX team. Topics include performance tuning the AndroidX Gradle builds using configuration caching, local caching and remote caching, as well as tracking API compatibility using...",
                summary = "In this episode, Tor and Romain chat with Aurimas Liutikas from the AndroidX team. Topics include performance tuning the AndroidX Gradle builds using configuration caching, local caching and remote caching, as well as tracking API compatibility using the Metalava tool. Aurimas, Romain and Tor   Romain: @romainguy, threads.net/@romainguy, romainguy@androiddev.social Tor: threads.net/@tor.norbye and tornorbye@androiddev.social Aurimas: androiddev.social/@Aurimas and www.liutikas.net/blog-posts   Catch videos on YouTube → https://goo.gle/adb-podcast    Subscribe to Android Developers  → https://goo.gle/AndroidDevs  ",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[2].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "https://freakonomics.com",
                title = "4. Does All Creativity Come From Pain?",
                subTitle = "Also: is life precious because it’s finite? This episode originally aired on June 7, 2020.",
                summary = "Also: is life precious because it’s finite? This episode originally aired on June 7, 2020.",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[3].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "https://audioboom.com/posts/8634883",
                title = "565: No Such Thing As Tickling A Monk",
                summary = "\u003cdiv\u003eLive from Wellington, Dan, James, Anna, and special guest Leon \u0027Buttons\u0027 Kirkbeck discuss Buddha, batons, balls of rice and bad kitties.\u003cbr\u003e\n\u003cbr\u003e\nVisit \u003ca href\u003d\"http://nosuchthingasafish.com\"\u003enosuchthingasafish.com\u003c/a\u003e for news about live shows, merchandise and more episodes. \u003cbr\u003e\n\u003cbr\u003e\nJoin Club Fish for ad-free episodes and exclusive bonus content at \u003ca href\u003d\"http://apple.co/nosuchthingasafish\"\u003eapple.co/nosuchthingasafish\u003c/a\u003e or \u003ca href\u003d\"http://nosuchthingasafish.com/patreon\"\u003enosuchthingasafish.com/patreon\u003c/a\u003e\u003cbr\u003e\n\u003cbr\u003e\n\u003cbr\u003e\n\n\u003c/div\u003e\n",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[3].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "https://www.thisamericanlife.org/198/how-to-win-friends-and-influence-people",
                title = "198: How to Win Friends and Influence People",
                subTitle = "People climbing to be number one. How do they do it?",
                summary = "People climbing to be number one. How do they do it? What is the fundamental difference between us and them?\n\n\nPrologue: Ira Glass talks with Paul Feig, who, as a sixth-grader, read the Dale Carnegie classic How to Win Friends and Influence People at the urging of his father. He found that afterward, he had a bleaker understanding of human nature—and even fewer friends than when he started. (9 minutes)\n\nAct One: David Sedaris has this instructive tale of how, as a boy, with the help of his dad, he tried to bridge the chasm that divides the popular kid from the unpopular — with the sorts of results that perhaps you might anticipate. (14 minutes)\n\nAct Two: After the September 11th attacks on the World Trade Center and the Pentagon, U.S. diplomats had to start working the phones to assemble a coalition of nations to combat this new threat. Some of the calls, you get the feeling, were not the easiest to make. Writer and performer Tami Sagher imagines what those calls were like. (6 minutes)\n\nAct Three: To prove this simple point—a familiar one to readers of any women\u0027s magazines—we have this true story of moral instruction, told by Luke Burbank in Seattle, about a guy he met on a plane dressed in a hand-sewn Superman costume. (13 minutes)\n\nAct Four: Jonathan Goldstein with a story about what it\u0027s like to date Lois Lane when she\u0027s on the rebound from Superman. (13 minutes)",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[3].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "https://www.thisamericanlife.org/699/fiasco",
                title = "699: Fiasco!",
                subTitle = "We leave the normal realm of human error and enter the territory of huge breakdowns.",
                summary = "We leave the normal realm of human error and enter the territory of huge breakdowns.\n\n\nPrologue: \n\tJack Hitt tells the story of a small-town production of Peter Pan in which all the usual boundaries between the audience and actors dissolve entirely. (6 minutes)\n\nAct One: Jack Hitt\u0027s Peter Pan story continues. (18 minutes)\n\nAct Two: The first day on the job inevitably means mistakes, mishaps, and sometimes, fiascos. A true story, told by a former rookie cop. (13 minutes)\n\nAct Three: Comedian Mike Birbiglia talks about the time he ruined a cancer charity event by giving the worst performance of his life. Here\u0027s a hint: He improvised. About cancer. (10 minutes)\n\nAct Four: Journalist Margy Rochlin on her first big assignment to do a celebrity interview: Moon Unit Zappa in 1982. Midway through the interview: fiasco! (7 minutes)",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[3].podcast
            ),
            userData = userData
        ),
        UserEpisode(
            episode = Episode(
                uri = "https://www.thisamericanlife.org/850/my-sweater",
                title = "850: If You Want to Destroy My Sweater, Hold This Thread as I Walk Away",
                subTitle = "The tiny thing that unravels your world.",
                summary = "The tiny thing that unravels your world.\n\n\nPrologue: Ira talks to Chris Benderev, whose high school years were completely upended by an impromptu thing his teacher said. (8 minutes)\n\nAct One: For Producer Lilly Sullivan, there’s one story about her parents that defines how she sees them, their family, and their history. She finds out it might be wrong. (27 minutes)\n\nAct Two: For years, Mike Comite has replayed in his head the moment when he and his bandmate blew their shot of making it as musicians. He sets out to uncover how it all went awry. (13 minutes)\n\nAct Three: Six million Syrians fled the country after the start of its civil war. A few weeks ago, one woman watched from afar as everything in her home country changed forever – again. (9 minutes)",
                published = LocalDateTime(
                    year = 2024,
                    monthNumber = 12,
                    dayOfMonth = 13,
                    hour = 10,
                    minute = 34,
                    second = 21,
                    nanosecond = 0
                ).toInstant( TimeZone.UTC ),
                podcast = podcasts[3].podcast
            ),
            userData = userData
        ),
    )
}