SET DEFINE OFF;
Insert into DB_CORE.SE_NOTIFICATIONS_TYPES
   (NOTIFICATION_TYPE_ID, NAME, ICON, ICON_COLOR)
 Values
   (1, 'Notification', 'fa fa-info-circle', '#4dabf7');
Insert into DB_CORE.SE_NOTIFICATIONS_TYPES
   (NOTIFICATION_TYPE_ID, NAME, ICON, ICON_COLOR)
 Values
   (2, 'Alert', 'fa fa-exclamation-triangle', '#ffd43b');
Insert into DB_CORE.SE_NOTIFICATIONS_TYPES
   (NOTIFICATION_TYPE_ID, NAME, ICON, ICON_COLOR)
 Values
   (3, 'Important', 'fa fa-hand-paper', '#ea4c89');
COMMIT;
