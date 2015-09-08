DELETE FROM `accounts`;
INSERT INTO `accounts` (`login`, `password`, `email`, `created_time`, `lastactive`, `accessLevel`, `lastIP`, `lastServer`, `lastChar`, `key1`, `key2`) VALUES
  ('ark', '123', NULL, '2015-01-18 04:31:08', 1424702071, 0, '127.0.0.1', 1, 1, -206342282, 1893542898),
  ('test', '123', NULL, '2015-02-23 22:45:23', 0, 0, NULL, 1, 0, 0, 0);

DELETE FROM `characters`;
INSERT INTO `characters` (`charId`, `account`, `charName`, `accessLevel`, `x`, `y`, `lvl`, `face`, `hairColor`, `hairStyle`, `sex`, `title`, `lastAccess`, `createDate`, `onlineTime`, `del`) VALUES
  (1, 'ark', '=arksu=', 100, 53267, 32448, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0),
  (2, 'test', 'test', 10, 120, 140, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0);

INSERT INTO `items` (`id`, `itemId`, `objectId`, `x`, `y`, `q`, `amount`, `stage`, `ticks`, `ticksTotal`) VALUES
  (50, 19, 1, 0, 0, 10, 0, 0, 0, 0),
  (51, 17, 1, 1, 1, 10, 0, 0, 0, 0);