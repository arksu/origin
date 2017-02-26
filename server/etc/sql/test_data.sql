DELETE FROM `accounts`;
INSERT INTO `accounts` (`login`, `password`, `email`, `created_time`, `lastactive`, `accessLevel`, `lastIP`, `lastServer`, `lastChar`, `key1`, `key2`) VALUES
  ('ark', '123', NULL, '2015-01-17 23:31:08', 1471452044, 0, '127.0.0.1', 1, 1, -128406775, -1792859738),
  ('test', '123', NULL, '2015-02-23 17:45:23', 1470565970, 0, '127.0.0.1', 1, 2, 1198274658, -1414135867);

DELETE FROM `characters`;
INSERT INTO `characters` (`charId`, `account`, `charName`, `accessLevel`, `x`, `y`, `lvl`, `face`, `hairColor`, `hairStyle`, `sex`, `title`, `lastAccess`, `createDate`, `onlineTime`, `del`) VALUES
  (1, 'ark', '=arksu=', 100, 52577, 33086, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0),
  (2, 'test', 'test', 10, 52709, 33165, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0);

INSERT INTO `items` (`id`, `itemId`, `inventoryId`, `x`, `y`, `q`, `amount`, `stage`, `ticks`, `ticksTotal`) VALUES
  (50, 19, 1, 0, 0, 10, 0, 0, 0, 0),
  (51, 17, 1, 1, 1, 10, 0, 0, 0, 0);