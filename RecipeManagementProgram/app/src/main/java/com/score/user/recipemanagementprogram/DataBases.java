package com.score.user.recipemanagementprogram;

import android.provider.BaseColumns;

public class DataBases
{
    public static final class CreateDB implements BaseColumns
    {
        public static final String NAME = "name";
        public static final String PRICE = "price";
        public static final String WEIGHT = "weight";
        public static final String PRICEPERGRAM = "pricePerGram";
        public static final String _TABLENAME = "costTable";
        public static final String USES = "uses";
        public static final String _CREATE =
            "create table if not exists "+_TABLENAME+"("
                    +NAME+" string primary key, "
                    +PRICE+" text not null , "
                    +WEIGHT+" text not null , "
                    +PRICEPERGRAM+" text not null , "
                    +USES+" text not null);";
    }
}
