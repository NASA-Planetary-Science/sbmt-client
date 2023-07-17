package edu.jhuapl.sbmt.core.client;

public enum Mission
	{
    	APL_INTERNAL_NIGHTLY("b1bc7ec", false),
		APL_INTERNAL("b1bc7ed", false),
		PUBLIC_RELEASE("3ee38f0", true),
		TEST_APL_INTERNAL("fb404a7", false),
		TEST_PUBLIC_RELEASE("a1a32b4", true),
		HAYABUSA2_DEV("133314b", false),
//		HAYABUSA2_STAGE("244425c", false),
		HAYABUSA2_DEPLOY("355536d", false),
		OSIRIS_REX("7cd84586", false),
//		OSIRIS_REX_STAGE("7cd84587", false),
        OSIRIS_REX_DEPLOY("7cd84588", false),
        OSIRIS_REX_TEST("h887aa63", false),
		OSIRIS_REX_MIRROR_DEPLOY("7cd84589", false),
		NH_DEPLOY("8ff86312", false),
		DART_DEV("9da75292", false),
		DART_DEPLOY("9da75293", false),
        DART_TEST("8f449edc", false),
        DART_STAGE("afac11cb", false),
		STAGE_APL_INTERNAL("f7e441b", false),
		STAGE_PUBLIC_RELEASE("8cc8e12", true),
		MEGANE_DEV("9da85292", false),
		MEGANE_DEPLOY("9da85293", false),
        MEGANE_TEST("8f549edc", false),
        MEGANE_STAGE("afad11cb", false),;

		private final String hashedName;
        private final boolean publishedDataOnly;

		Mission(String hashedName, boolean publishedDataOnly)
		{
			this.hashedName = hashedName;
            this.publishedDataOnly = publishedDataOnly;
		}

		public String getHashedName()
		{
			return hashedName;
		}

		public boolean isPublishedDataOnly()
		{
		    return publishedDataOnly;
		}

		public static Mission getMissionForName(String name)
		{
		    for (Mission msn : values())
		    {
		        if (name.equals(msn.hashedName))
		            return msn;
		    }
		    return null;
		}
	}