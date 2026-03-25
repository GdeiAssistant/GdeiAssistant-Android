package cn.gdeiassistant.model

data class LocalizedProfileCatalog(
    val unselectedLabel: String,
    val otherLabel: String,
    val defaultOptions: ProfileOptions
) {
    companion object {
        fun currentCatalog(): LocalizedProfileCatalog = catalogForLocale(AppLocaleSupport.currentLocale())

        fun catalogForLocale(locale: String): LocalizedProfileCatalog {
            val normalizedLocale = AppLocaleSupport.normalizeLocale(locale)
            val labels = localizedLabels[normalizedLocale] ?: localizedLabels.getValue("zh-CN")
            val unselected = labels.getValue("unselected")
            return LocalizedProfileCatalog(
                unselectedLabel = unselected,
                otherLabel = labels.getValue("marketplace.other"),
                defaultOptions = ProfileOptions(
                    faculties = facultyDefinitions.map { definition ->
                        ProfileFacultyOption(
                            code = definition.code,
                            label = labels.getValue(definition.labelKey),
                            majors = definition.majorCodes.map { code ->
                                ProfileMajorOption(code = code, label = labels.getValue(code))
                            }
                        )
                    },
                    marketplaceItemTypes = marketplaceItemTypeCodes.mapIndexed { index, code ->
                        ProfileDictionaryOption(code = index, label = labels.getValue(code))
                    },
                    lostFoundItemTypes = lostFoundItemTypeCodes.mapIndexed { index, code ->
                        ProfileDictionaryOption(code = index, label = labels.getValue(code))
                    },
                    lostFoundModes = lostFoundModeCodes.mapIndexed { index, code ->
                        ProfileDictionaryOption(code = index, label = labels.getValue(code))
                    }
                )
            )
        }

        private val facultyDefinitions = listOf(
            FacultyDefinition(0, "unselected", listOf("unselected")),
            FacultyDefinition(1, "faculty.education", listOf("unselected", "education", "preschool_education", "primary_education", "special_education")),
            FacultyDefinition(2, "faculty.politics_law", listOf("unselected", "law", "ideological_political_education", "social_work")),
            FacultyDefinition(3, "faculty.chinese", listOf("unselected", "chinese_language_literature", "history", "secretarial_studies")),
            FacultyDefinition(4, "faculty.mathematics", listOf("unselected", "mathematics_applied_mathematics", "information_computing_science", "statistics")),
            FacultyDefinition(5, "faculty.foreign_languages", listOf("unselected", "english", "business_english", "japanese", "translation")),
            FacultyDefinition(6, "faculty.physics_information", listOf("unselected", "physics", "electronic_information_engineering", "communication_engineering")),
            FacultyDefinition(7, "faculty.chemistry", listOf("unselected", "chemistry", "applied_chemistry", "materials_chemistry")),
            FacultyDefinition(8, "faculty.biology_food", listOf("unselected", "biological_science", "biotechnology", "food_science_engineering")),
            FacultyDefinition(9, "faculty.physical_education", listOf("unselected", "physical_education", "social_sports_guidance_management")),
            FacultyDefinition(10, "faculty.fine_arts", listOf("unselected", "fine_arts", "visual_communication_design", "environmental_design")),
            FacultyDefinition(11, "faculty.computer_science", listOf("unselected", "software_engineering", "network_engineering", "computer_science_technology", "internet_of_things_engineering")),
            FacultyDefinition(12, "faculty.music", listOf("unselected", "musicology", "music_performance", "dance")),
            FacultyDefinition(13, "faculty.teacher_training", listOf("unselected", "education", "educational_technology")),
            FacultyDefinition(14, "faculty.continuing_education", listOf("unselected", "chinese_language_literature", "preschool_education", "public_administration")),
            FacultyDefinition(15, "faculty.online_education", listOf("unselected", "computer_science_technology", "business_administration", "accounting")),
            FacultyDefinition(16, "faculty.marxism", listOf("unselected", "ideological_political_education", "marxist_theory"))
        )

        private val marketplaceItemTypeCodes = listOf(
            "marketplace.campus_transportation", "marketplace.phone", "marketplace.computer",
            "marketplace.digital_accessories", "marketplace.digital_devices", "marketplace.home_appliances",
            "marketplace.sports_fitness", "marketplace.clothing_accessories", "marketplace.books_textbooks",
            "marketplace.rental", "marketplace.lifestyle_entertainment", "marketplace.other"
        )

        private val lostFoundItemTypeCodes = listOf(
            "lostfound.phone", "lostfound.campus_card", "lostfound.id_card", "lostfound.bank_card",
            "lostfound.book", "lostfound.keys", "lostfound.bag", "lostfound.clothing",
            "lostfound.campus_transportation", "lostfound.sports_fitness", "lostfound.digital_accessories",
            "lostfound.other"
        )

        private val lostFoundModeCodes = listOf("lostfound.notice_seek", "lostfound.notice_found")

        private val localizedLabels = mapOf(
            "zh-CN" to mapOf(
                "unselected" to "未选择", "faculty.education" to "教育学院", "faculty.politics_law" to "政法系",
                "faculty.chinese" to "中文系", "faculty.mathematics" to "数学系", "faculty.foreign_languages" to "外语系",
                "faculty.physics_information" to "物理与信息工程系", "faculty.chemistry" to "化学系",
                "faculty.biology_food" to "生物与食品工程学院", "faculty.physical_education" to "体育学院",
                "faculty.fine_arts" to "美术学院", "faculty.computer_science" to "计算机科学系",
                "faculty.music" to "音乐系", "faculty.teacher_training" to "教师研修学院",
                "faculty.continuing_education" to "成人教育学院", "faculty.online_education" to "网络教育学院",
                "faculty.marxism" to "马克思主义学院", "education" to "教育学", "preschool_education" to "学前教育",
                "primary_education" to "小学教育", "special_education" to "特殊教育", "law" to "法学",
                "ideological_political_education" to "思想政治教育", "social_work" to "社会工作",
                "chinese_language_literature" to "汉语言文学", "history" to "历史学", "secretarial_studies" to "秘书学",
                "mathematics_applied_mathematics" to "数学与应用数学", "information_computing_science" to "信息与计算科学",
                "statistics" to "统计学", "english" to "英语", "business_english" to "商务英语", "japanese" to "日语",
                "translation" to "翻译", "physics" to "物理学", "electronic_information_engineering" to "电子信息工程",
                "communication_engineering" to "通信工程", "chemistry" to "化学", "applied_chemistry" to "应用化学",
                "materials_chemistry" to "材料化学", "biological_science" to "生物科学", "biotechnology" to "生物技术",
                "food_science_engineering" to "食品科学与工程", "physical_education" to "体育教育",
                "social_sports_guidance_management" to "社会体育指导与管理", "fine_arts" to "美术学",
                "visual_communication_design" to "视觉传达设计", "environmental_design" to "环境设计",
                "software_engineering" to "软件工程", "network_engineering" to "网络工程",
                "computer_science_technology" to "计算机科学与技术", "internet_of_things_engineering" to "物联网工程",
                "musicology" to "音乐学", "music_performance" to "音乐表演", "dance" to "舞蹈学",
                "educational_technology" to "教育技术学", "public_administration" to "行政管理",
                "business_administration" to "工商管理", "accounting" to "会计学", "marxist_theory" to "马克思主义理论",
                "marketplace.campus_transportation" to "校园代步", "marketplace.phone" to "手机", "marketplace.computer" to "电脑",
                "marketplace.digital_accessories" to "数码配件", "marketplace.digital_devices" to "数码", "marketplace.home_appliances" to "电器",
                "marketplace.sports_fitness" to "运动健身", "marketplace.clothing_accessories" to "衣物伞帽",
                "marketplace.books_textbooks" to "图书教材", "marketplace.rental" to "租赁",
                "marketplace.lifestyle_entertainment" to "生活娱乐", "marketplace.other" to "其他",
                "lostfound.phone" to "手机", "lostfound.campus_card" to "校园卡", "lostfound.id_card" to "身份证",
                "lostfound.bank_card" to "银行卡", "lostfound.book" to "书", "lostfound.keys" to "钥匙",
                "lostfound.bag" to "包包", "lostfound.clothing" to "衣帽", "lostfound.campus_transportation" to "校园代步",
                "lostfound.sports_fitness" to "运动健身", "lostfound.digital_accessories" to "数码配件", "lostfound.other" to "其他",
                "lostfound.notice_seek" to "寻物启事", "lostfound.notice_found" to "失物招领"
            ),
            "zh-HK" to mapOf(
                "unselected" to "未選擇", "faculty.education" to "教育學院", "faculty.politics_law" to "政法系",
                "faculty.chinese" to "中文系", "faculty.mathematics" to "數學系", "faculty.foreign_languages" to "外語系",
                "faculty.physics_information" to "物理與信息工程系", "faculty.chemistry" to "化學系",
                "faculty.biology_food" to "生物與食品工程學院", "faculty.physical_education" to "體育學院",
                "faculty.fine_arts" to "美術學院", "faculty.computer_science" to "計算機科學系",
                "faculty.music" to "音樂系", "faculty.teacher_training" to "教師研修學院",
                "faculty.continuing_education" to "成人教育學院", "faculty.online_education" to "網絡教育學院",
                "faculty.marxism" to "馬克思主義學院", "education" to "教育學", "preschool_education" to "學前教育",
                "primary_education" to "小學教育", "special_education" to "特殊教育", "law" to "法學",
                "ideological_political_education" to "思想政治教育", "social_work" to "社會工作",
                "chinese_language_literature" to "漢語言文學", "history" to "歷史學", "secretarial_studies" to "秘書學",
                "mathematics_applied_mathematics" to "數學與應用數學", "information_computing_science" to "信息與計算科學",
                "statistics" to "統計學", "english" to "英語", "business_english" to "商務英語", "japanese" to "日語",
                "translation" to "翻譯", "physics" to "物理學", "electronic_information_engineering" to "電子信息工程",
                "communication_engineering" to "通信工程", "chemistry" to "化學", "applied_chemistry" to "應用化學",
                "materials_chemistry" to "材料化學", "biological_science" to "生物科學", "biotechnology" to "生物技術",
                "food_science_engineering" to "食品科學與工程", "physical_education" to "體育教育",
                "social_sports_guidance_management" to "社會體育指導與管理", "fine_arts" to "美術學",
                "visual_communication_design" to "視覺傳達設計", "environmental_design" to "環境設計",
                "software_engineering" to "軟件工程", "network_engineering" to "網絡工程",
                "computer_science_technology" to "計算機科學與技術", "internet_of_things_engineering" to "物聯網工程",
                "musicology" to "音樂學", "music_performance" to "音樂表演", "dance" to "舞蹈學",
                "educational_technology" to "教育技術學", "public_administration" to "行政管理",
                "business_administration" to "工商管理", "accounting" to "會計學", "marxist_theory" to "馬克思主義理論",
                "marketplace.campus_transportation" to "校園代步", "marketplace.phone" to "手機", "marketplace.computer" to "電腦",
                "marketplace.digital_accessories" to "數碼配件", "marketplace.digital_devices" to "數碼", "marketplace.home_appliances" to "電器",
                "marketplace.sports_fitness" to "運動健身", "marketplace.clothing_accessories" to "衣物傘帽",
                "marketplace.books_textbooks" to "圖書教材", "marketplace.rental" to "租賃",
                "marketplace.lifestyle_entertainment" to "生活娛樂", "marketplace.other" to "其他",
                "lostfound.phone" to "手機", "lostfound.campus_card" to "校園卡", "lostfound.id_card" to "身份證",
                "lostfound.bank_card" to "銀行卡", "lostfound.book" to "書", "lostfound.keys" to "鎖匙",
                "lostfound.bag" to "包包", "lostfound.clothing" to "衣帽", "lostfound.campus_transportation" to "校園代步",
                "lostfound.sports_fitness" to "運動健身", "lostfound.digital_accessories" to "數碼配件", "lostfound.other" to "其他",
                "lostfound.notice_seek" to "尋物啟事", "lostfound.notice_found" to "失物招領"
            ),
            "zh-TW" to mapOf(
                "unselected" to "未選擇", "faculty.education" to "教育學院", "faculty.politics_law" to "政法系",
                "faculty.chinese" to "中文系", "faculty.mathematics" to "數學系", "faculty.foreign_languages" to "外語系",
                "faculty.physics_information" to "物理與資訊工程系", "faculty.chemistry" to "化學系",
                "faculty.biology_food" to "生物與食品工程學院", "faculty.physical_education" to "體育學院",
                "faculty.fine_arts" to "美術學院", "faculty.computer_science" to "計算機科學系",
                "faculty.music" to "音樂系", "faculty.teacher_training" to "教師研修學院",
                "faculty.continuing_education" to "成人教育學院", "faculty.online_education" to "網路教育學院",
                "faculty.marxism" to "馬克思主義學院", "education" to "教育學", "preschool_education" to "學前教育",
                "primary_education" to "國小教育", "special_education" to "特殊教育", "law" to "法學",
                "ideological_political_education" to "思想政治教育", "social_work" to "社會工作",
                "chinese_language_literature" to "漢語言文學", "history" to "歷史學", "secretarial_studies" to "秘書學",
                "mathematics_applied_mathematics" to "數學與應用數學", "information_computing_science" to "資訊與計算科學",
                "statistics" to "統計學", "english" to "英語", "business_english" to "商務英語", "japanese" to "日語",
                "translation" to "翻譯", "physics" to "物理學", "electronic_information_engineering" to "電子資訊工程",
                "communication_engineering" to "通訊工程", "chemistry" to "化學", "applied_chemistry" to "應用化學",
                "materials_chemistry" to "材料化學", "biological_science" to "生物科學", "biotechnology" to "生物技術",
                "food_science_engineering" to "食品科學與工程", "physical_education" to "體育教育",
                "social_sports_guidance_management" to "社會體育指導與管理", "fine_arts" to "美術學",
                "visual_communication_design" to "視覺傳達設計", "environmental_design" to "環境設計",
                "software_engineering" to "軟體工程", "network_engineering" to "網路工程",
                "computer_science_technology" to "計算機科學與技術", "internet_of_things_engineering" to "物聯網工程",
                "musicology" to "音樂學", "music_performance" to "音樂表演", "dance" to "舞蹈學",
                "educational_technology" to "教育技術學", "public_administration" to "行政管理",
                "business_administration" to "工商管理", "accounting" to "會計學", "marxist_theory" to "馬克思主義理論",
                "marketplace.campus_transportation" to "校園代步", "marketplace.phone" to "手機", "marketplace.computer" to "電腦",
                "marketplace.digital_accessories" to "數位配件", "marketplace.digital_devices" to "數位", "marketplace.home_appliances" to "家電",
                "marketplace.sports_fitness" to "運動健身", "marketplace.clothing_accessories" to "衣物傘帽",
                "marketplace.books_textbooks" to "圖書教材", "marketplace.rental" to "租賃",
                "marketplace.lifestyle_entertainment" to "生活娛樂", "marketplace.other" to "其他",
                "lostfound.phone" to "手機", "lostfound.campus_card" to "校園卡", "lostfound.id_card" to "身分證",
                "lostfound.bank_card" to "銀行卡", "lostfound.book" to "書", "lostfound.keys" to "鑰匙",
                "lostfound.bag" to "包包", "lostfound.clothing" to "衣帽", "lostfound.campus_transportation" to "校園代步",
                "lostfound.sports_fitness" to "運動健身", "lostfound.digital_accessories" to "數位配件", "lostfound.other" to "其他",
                "lostfound.notice_seek" to "尋物啟事", "lostfound.notice_found" to "失物招領"
            ),
            "en" to mapOf(
                "unselected" to "Not selected", "faculty.education" to "School of Education", "faculty.politics_law" to "Department of Politics and Law",
                "faculty.chinese" to "Department of Chinese", "faculty.mathematics" to "Department of Mathematics", "faculty.foreign_languages" to "Department of Foreign Languages",
                "faculty.physics_information" to "Department of Physics and Information Engineering", "faculty.chemistry" to "Department of Chemistry",
                "faculty.biology_food" to "School of Biology and Food Engineering", "faculty.physical_education" to "School of Physical Education",
                "faculty.fine_arts" to "School of Fine Arts", "faculty.computer_science" to "Department of Computer Science",
                "faculty.music" to "Department of Music", "faculty.teacher_training" to "Teacher Training Institute",
                "faculty.continuing_education" to "Continuing Education Institute", "faculty.online_education" to "Online Education Institute",
                "faculty.marxism" to "School of Marxism", "education" to "Education", "preschool_education" to "Preschool Education",
                "primary_education" to "Primary Education", "special_education" to "Special Education", "law" to "Law",
                "ideological_political_education" to "Ideological and Political Education", "social_work" to "Social Work",
                "chinese_language_literature" to "Chinese Language and Literature", "history" to "History", "secretarial_studies" to "Secretarial Studies",
                "mathematics_applied_mathematics" to "Mathematics and Applied Mathematics", "information_computing_science" to "Information and Computing Science",
                "statistics" to "Statistics", "english" to "English", "business_english" to "Business English", "japanese" to "Japanese",
                "translation" to "Translation", "physics" to "Physics", "electronic_information_engineering" to "Electronic Information Engineering",
                "communication_engineering" to "Communication Engineering", "chemistry" to "Chemistry", "applied_chemistry" to "Applied Chemistry",
                "materials_chemistry" to "Materials Chemistry", "biological_science" to "Biological Science", "biotechnology" to "Biotechnology",
                "food_science_engineering" to "Food Science and Engineering", "physical_education" to "Physical Education",
                "social_sports_guidance_management" to "Social Sports Guidance and Management", "fine_arts" to "Fine Arts",
                "visual_communication_design" to "Visual Communication Design", "environmental_design" to "Environmental Design",
                "software_engineering" to "Software Engineering", "network_engineering" to "Network Engineering",
                "computer_science_technology" to "Computer Science and Technology", "internet_of_things_engineering" to "Internet of Things Engineering",
                "musicology" to "Musicology", "music_performance" to "Music Performance", "dance" to "Dance",
                "educational_technology" to "Educational Technology", "public_administration" to "Public Administration",
                "business_administration" to "Business Administration", "accounting" to "Accounting", "marxist_theory" to "Marxist Theory",
                "marketplace.campus_transportation" to "Campus Transportation", "marketplace.phone" to "Phone", "marketplace.computer" to "Computer",
                "marketplace.digital_accessories" to "Digital Accessories", "marketplace.digital_devices" to "Digital Devices", "marketplace.home_appliances" to "Home Appliances",
                "marketplace.sports_fitness" to "Sports and Fitness", "marketplace.clothing_accessories" to "Clothing and Accessories",
                "marketplace.books_textbooks" to "Books and Textbooks", "marketplace.rental" to "Rental",
                "marketplace.lifestyle_entertainment" to "Lifestyle and Entertainment", "marketplace.other" to "Other",
                "lostfound.phone" to "Phone", "lostfound.campus_card" to "Campus Card", "lostfound.id_card" to "ID Card",
                "lostfound.bank_card" to "Bank Card", "lostfound.book" to "Book", "lostfound.keys" to "Keys",
                "lostfound.bag" to "Bag", "lostfound.clothing" to "Clothing", "lostfound.campus_transportation" to "Campus Transportation",
                "lostfound.sports_fitness" to "Sports and Fitness", "lostfound.digital_accessories" to "Digital Accessories", "lostfound.other" to "Other",
                "lostfound.notice_seek" to "Lost Item Notice", "lostfound.notice_found" to "Found Item Notice"
            ),
            "ja" to mapOf(
                "unselected" to "未選択", "faculty.education" to "教育学院", "faculty.politics_law" to "政治法律学科",
                "faculty.chinese" to "中国語学科", "faculty.mathematics" to "数学科", "faculty.foreign_languages" to "外国語学科",
                "faculty.physics_information" to "物理情報工学科", "faculty.chemistry" to "化学科",
                "faculty.biology_food" to "生物食品工学学院", "faculty.physical_education" to "体育学院",
                "faculty.fine_arts" to "美術学院", "faculty.computer_science" to "計算機科学科",
                "faculty.music" to "音楽学科", "faculty.teacher_training" to "教員研修学院",
                "faculty.continuing_education" to "成人教育学院", "faculty.online_education" to "オンライン教育学院",
                "faculty.marxism" to "マルクス主義学院", "education" to "教育学", "preschool_education" to "幼児教育",
                "primary_education" to "初等教育", "special_education" to "特別支援教育", "law" to "法学",
                "ideological_political_education" to "思想政治教育", "social_work" to "社会福祉",
                "chinese_language_literature" to "中国語中国文学", "history" to "歴史学", "secretarial_studies" to "秘書学",
                "mathematics_applied_mathematics" to "数学・応用数学", "information_computing_science" to "情報計算科学",
                "statistics" to "統計学", "english" to "英語", "business_english" to "ビジネス英語", "japanese" to "日本語",
                "translation" to "翻訳", "physics" to "物理学", "electronic_information_engineering" to "電子情報工学",
                "communication_engineering" to "通信工学", "chemistry" to "化学", "applied_chemistry" to "応用化学",
                "materials_chemistry" to "材料化学", "biological_science" to "生物科学", "biotechnology" to "バイオテクノロジー",
                "food_science_engineering" to "食品科学工学", "physical_education" to "体育教育",
                "social_sports_guidance_management" to "社会体育指導管理", "fine_arts" to "美術学",
                "visual_communication_design" to "ビジュアルコミュニケーションデザイン", "environmental_design" to "環境デザイン",
                "software_engineering" to "ソフトウェア工学", "network_engineering" to "ネットワーク工学",
                "computer_science_technology" to "計算機科学技術", "internet_of_things_engineering" to "IoT工学",
                "musicology" to "音楽学", "music_performance" to "音楽パフォーマンス", "dance" to "舞踊学",
                "educational_technology" to "教育技術学", "public_administration" to "行政管理",
                "business_administration" to "経営学", "accounting" to "会計学", "marxist_theory" to "マルクス主義理論",
                "marketplace.campus_transportation" to "キャンパス移動", "marketplace.phone" to "携帯電話", "marketplace.computer" to "パソコン",
                "marketplace.digital_accessories" to "デジタル周辺機器", "marketplace.digital_devices" to "デジタル機器", "marketplace.home_appliances" to "家電",
                "marketplace.sports_fitness" to "スポーツ・フィットネス", "marketplace.clothing_accessories" to "衣類・小物",
                "marketplace.books_textbooks" to "書籍・教科書", "marketplace.rental" to "賃貸",
                "marketplace.lifestyle_entertainment" to "生活・娯楽", "marketplace.other" to "その他",
                "lostfound.phone" to "携帯電話", "lostfound.campus_card" to "学生証", "lostfound.id_card" to "身分証",
                "lostfound.bank_card" to "銀行カード", "lostfound.book" to "本", "lostfound.keys" to "鍵",
                "lostfound.bag" to "かばん", "lostfound.clothing" to "衣類", "lostfound.campus_transportation" to "キャンパス移動",
                "lostfound.sports_fitness" to "スポーツ・フィットネス", "lostfound.digital_accessories" to "デジタル周辺機器",
                "lostfound.other" to "その他", "lostfound.notice_seek" to "落とし物捜索", "lostfound.notice_found" to "拾得物案内"
            ),
            "ko" to mapOf(
                "unselected" to "선택 안 함", "faculty.education" to "교육대학", "faculty.politics_law" to "정치법학과",
                "faculty.chinese" to "중문과", "faculty.mathematics" to "수학과", "faculty.foreign_languages" to "외국어과",
                "faculty.physics_information" to "물리정보공학과", "faculty.chemistry" to "화학과",
                "faculty.biology_food" to "생물식품공학대학", "faculty.physical_education" to "체육대학",
                "faculty.fine_arts" to "미술대학", "faculty.computer_science" to "컴퓨터과학과",
                "faculty.music" to "음악과", "faculty.teacher_training" to "교사연수대학",
                "faculty.continuing_education" to "평생교육대학", "faculty.online_education" to "원격교육대학",
                "faculty.marxism" to "마르크스주의대학", "education" to "교육학", "preschool_education" to "유아교육",
                "primary_education" to "초등교육", "special_education" to "특수교육", "law" to "법학",
                "ideological_political_education" to "사상정치교육", "social_work" to "사회복지",
                "chinese_language_literature" to "중국어문학", "history" to "역사학", "secretarial_studies" to "비서학",
                "mathematics_applied_mathematics" to "수학 및 응용수학", "information_computing_science" to "정보계산과학",
                "statistics" to "통계학", "english" to "영어", "business_english" to "비즈니스 영어", "japanese" to "일본어",
                "translation" to "번역", "physics" to "물리학", "electronic_information_engineering" to "전자정보공학",
                "communication_engineering" to "통신공학", "chemistry" to "화학", "applied_chemistry" to "응용화학",
                "materials_chemistry" to "재료화학", "biological_science" to "생물과학", "biotechnology" to "생명공학",
                "food_science_engineering" to "식품과학공학", "physical_education" to "체육교육",
                "social_sports_guidance_management" to "사회체육지도관리", "fine_arts" to "미술학",
                "visual_communication_design" to "시각디자인", "environmental_design" to "환경디자인",
                "software_engineering" to "소프트웨어공학", "network_engineering" to "네트워크공학",
                "computer_science_technology" to "컴퓨터과학기술", "internet_of_things_engineering" to "사물인터넷공학",
                "musicology" to "음악학", "music_performance" to "음악공연", "dance" to "무용학",
                "educational_technology" to "교육기술학", "public_administration" to "행정관리",
                "business_administration" to "경영학", "accounting" to "회계학", "marxist_theory" to "마르크스주의 이론",
                "marketplace.campus_transportation" to "캠퍼스 이동수단", "marketplace.phone" to "휴대폰", "marketplace.computer" to "컴퓨터",
                "marketplace.digital_accessories" to "디지털 액세서리", "marketplace.digital_devices" to "디지털 기기", "marketplace.home_appliances" to "가전제품",
                "marketplace.sports_fitness" to "운동·피트니스", "marketplace.clothing_accessories" to "의류·잡화",
                "marketplace.books_textbooks" to "도서·교재", "marketplace.rental" to "임대",
                "marketplace.lifestyle_entertainment" to "생활·엔터테인먼트", "marketplace.other" to "기타",
                "lostfound.phone" to "휴대폰", "lostfound.campus_card" to "학생증", "lostfound.id_card" to "신분증",
                "lostfound.bank_card" to "은행카드", "lostfound.book" to "책", "lostfound.keys" to "열쇠",
                "lostfound.bag" to "가방", "lostfound.clothing" to "의류", "lostfound.campus_transportation" to "캠퍼스 이동수단",
                "lostfound.sports_fitness" to "운동·피트니스", "lostfound.digital_accessories" to "디지털 액세서리",
                "lostfound.other" to "기타", "lostfound.notice_seek" to "분실물 찾기", "lostfound.notice_found" to "습득물 안내"
            )
        )
    }
}

private data class FacultyDefinition(
    val code: Int,
    val labelKey: String,
    val majorCodes: List<String>
)
