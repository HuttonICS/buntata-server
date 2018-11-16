
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for attributes
-- ----------------------------
DROP TABLE IF EXISTS `attributes`;
CREATE TABLE `attributes`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attributes
-- ----------------------------

-- ----------------------------
-- Table structure for attributevalues
-- ----------------------------
DROP TABLE IF EXISTS `attributevalues`;
CREATE TABLE `attributevalues`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_id` int(11) NOT NULL,
  `attribute_id` int(11) NOT NULL,
  `value` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `node_id`(`node_id`) USING BTREE,
  INDEX `attribute_id`(`attribute_id`) USING BTREE,
  CONSTRAINT `attributevalues_ibfk_1` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `attributevalues_ibfk_2` FOREIGN KEY (`node_id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attributevalues
-- ----------------------------

-- ----------------------------
-- Table structure for datasources
-- ----------------------------
DROP TABLE IF EXISTS `datasources`;
CREATE TABLE `datasources`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `description` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `visibility` tinyint(1) NOT NULL DEFAULT 1,
  `version_number` int(11) NOT NULL DEFAULT 1,
  `data_provider` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `contact` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `show_key_name` tinyint(1) NOT NULL DEFAULT 1,
  `show_single_child` tinyint(1) NOT NULL DEFAULT 1,
  `icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size_no_video` bigint(20) NULL DEFAULT 0,
  `size_total` bigint(20) NULL DEFAULT 0,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datasources
-- ----------------------------

-- ----------------------------
-- Table structure for media
-- ----------------------------
DROP TABLE IF EXISTS `media`;
CREATE TABLE `media`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mediatype_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `description` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `internal_link` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `external_link` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `external_link_description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `copyright` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `mediatype_id`(`mediatype_id`) USING BTREE,
  CONSTRAINT `media_ibfk_1` FOREIGN KEY (`mediatype_id`) REFERENCES `mediatypes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of media
-- ----------------------------

-- ----------------------------
-- Table structure for mediatypes
-- ----------------------------
DROP TABLE IF EXISTS `mediatypes`;
CREATE TABLE `mediatypes`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mediatypes
-- ----------------------------
INSERT INTO `mediatypes` VALUES (1, 'Image', NULL, NULL);
INSERT INTO `mediatypes` VALUES (2, 'Video', NULL, NULL);

-- ----------------------------
-- Table structure for nodemedia
-- ----------------------------
DROP TABLE IF EXISTS `nodemedia`;
CREATE TABLE `nodemedia`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_id` int(11) NOT NULL,
  `media_id` int(11) NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `node_id`(`node_id`) USING BTREE,
  INDEX `media_id`(`media_id`) USING BTREE,
  CONSTRAINT `nodemedia_ibfk_1` FOREIGN KEY (`node_id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `nodemedia_ibfk_2` FOREIGN KEY (`media_id`) REFERENCES `media` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of nodemedia
-- ----------------------------

-- ----------------------------
-- Table structure for nodes
-- ----------------------------
DROP TABLE IF EXISTS `nodes`;
CREATE TABLE `nodes`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `datasource_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `description` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `datasource_id`(`datasource_id`) USING BTREE,
  CONSTRAINT `nodes_ibfk_1` FOREIGN KEY (`datasource_id`) REFERENCES `datasources` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of nodes
-- ----------------------------

-- ----------------------------
-- Table structure for relationships
-- ----------------------------
DROP TABLE IF EXISTS `relationships`;
CREATE TABLE `relationships`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent` int(11) NOT NULL,
  `child` int(11) NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent`(`parent`) USING BTREE,
  INDEX `child`(`child`) USING BTREE,
  CONSTRAINT `relationships_ibfk_1` FOREIGN KEY (`child`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `relationships_ibfk_2` FOREIGN KEY (`parent`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of relationships
-- ----------------------------

-- ----------------------------
-- Table structure for similarities
-- ----------------------------
DROP TABLE IF EXISTS `similarities`;
CREATE TABLE `similarities`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `node_a_id` int(11) NOT NULL,
  `node_b_id` int(11) NOT NULL,
  `created_on` datetime(0) NULL DEFAULT NULL,
  `updated_on` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `node_a_id`(`node_a_id`) USING BTREE,
  INDEX `node_b_id`(`node_b_id`) USING BTREE,
  CONSTRAINT `similarities_ibfk_1` FOREIGN KEY (`node_a_id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `similarities_ibfk_2` FOREIGN KEY (`node_b_id`) REFERENCES `nodes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of similarities
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
