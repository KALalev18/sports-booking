package com.kalalev.sports.content;

import java.util.List;
import java.util.Optional;

/**
 * The construction services shown in the home page sidebar. They are fixed marketing pages, so they
 * live in code rather than in the database.
 */
public final class ServiceCatalog {

    public record ServicePage(String slug, String name, String title, String intro, List<String> photos) {}

    private static final String INTRO = "Over the years we have designed and built facilities in many different "
            + "layouts, some of which you can see above. We build pitches with artificial or natural grass, "
            + "with fencing and lighting, across Bulgaria.";

    public static final List<String> OFFERS = List.of(
            "Professional artificial grass surfaces",
            "Covered stands for spectators",
            "Fencing systems for mini football",
            "Lighting systems for football pitches");

    private static final List<ServicePage> PAGES = List.of(
            new ServicePage(
                    "football",
                    "Football pitches",
                    "Design and construction of football pitches with artificial or natural grass",
                    INTRO,
                    List.of(
                            "https://centrecolonels.com/images/2021/6/21/web05312017_Evening_Aerials_00032.jpg",
                            "https://mumonarchs.com/images/2021/3/1/facilities_SOC_Duggins_aerial_lighted_DJI_0237_edited.jpg")),
            new ServicePage(
                    "tennis",
                    "Tennis courts",
                    "Design and construction of tennis courts with hard or acrylic surfaces",
                    INTRO,
                    List.of(
                            "https://www.versacourt.com/cmss_files/imagelibrary/Header_Images/header--tennis-courts.jpg",
                            "https://www.parrishkauai.com/wp-content/uploads/2013/08/Tennis-at-Hanalei-Bay-Resort-1.jpg",
                            "https://newhavenchargers.com/images/2019/11/18/Tennis_Courts_20190905_DJI_0709.jpg")),
            new ServicePage(
                    "lighting",
                    "Lighting",
                    "Design and installation of lighting for sports facilities",
                    INTRO,
                    List.of(
                            "https://www.zgsm-china.com/wp-content/uploads/2022/04/Football-Stadium.jpg",
                            "https://www.gmrenlights.com/images/proiettori-testata.jpg")),
            new ServicePage(
                    "flooring",
                    "Flooring",
                    "Design and installation of surfaces for sports facilities",
                    "We supply and install sports surfaces for indoor and outdoor sports: natural and artificial "
                            + "grass for football, hockey surfaces, tennis court surfaces and running tracks.",
                    List.of("https://www.eldesign.bg/storage1/images/buildings/item167/pic235/A.jpg")),
            new ServicePage(
                    "stands",
                    "Seats and stands",
                    "Design and construction of stands and seating",
                    INTRO,
                    List.of(
                            "https://www.ruse-sport.com/wp-content/uploads/Tribune-standard-3m4lev-vizualization-Temp0007.jpeg",
                            "http://stroeji.bg/data/user_products_images/1522400798/tn1_1522400798.jpg",
                            "https://sportlab.bg/uploads/img/255/statichna-tribuna-boiadjik-01.jpg")),
            new ServicePage(
                    "schools",
                    "For schools",
                    "Design and construction of sports facilities for schools",
                    INTRO,
                    List.of(
                            "https://images.squarespace-cdn.com/content/v1/5fdb620db585984fb1297f21/2f8af7b8-e56e-4f7e-98b0-d87481ac303c/Tilev_Architects_School_German_Sport_02.jpg?format=1000w",
                            "https://images.squarespace-cdn.com/content/v1/5fdb620db585984fb1297f21/ce5e4324-dd0d-42ab-a369-6cb89788c414/Tilev_Architects_School_German_Sport_01.jpg?format=1000w")));

    private ServiceCatalog() {}

    public static List<ServicePage> all() {
        return PAGES;
    }

    public static Optional<ServicePage> find(String slug) {
        return PAGES.stream().filter(page -> page.slug().equals(slug)).findFirst();
    }
}
