CREATE TABLE IF NOT EXISTS `tb_images` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `description` varchar(300) NOT NULL DEFAULT '',
    `tags` varchar(300) NOT NULL DEFAULT '',
    `original_uri` varchar(300) NOT NULL DEFAULT '',
    `uri` varchar(300) NOT NULL DEFAULT '',
    `owner_id` bigint(20) NOT NULL,
    `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES `tb_users`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8
