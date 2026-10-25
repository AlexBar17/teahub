package com.tea.teahub.fixture;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;

import java.math.BigDecimal;

public final class TeaTestData {

    private TeaTestData() {
    }

    public static final String GABA_NAME = "GABA Alishan";
    public static final String PUER_NAME = "Shu Pu-erh Menghai";
    public static final String LONGJING_NAME = "Longjing";

    public static final String TAIWAN = "China-Taiwan";
    public static final String CHINA = "China";

    public static final String ALISHAN = "Alishan";
    public static final String YUNNAN = "Yunnan";
    public static final String ZHEJIANG = "Zhejiang";

    public static final String GABA_NOTES =
        "GABA processed, creamy, calming effect";
    public static final String PUER_NOTES =
        "Earthy, woody, smooth body";
    public static final String LONGJING_NOTES =
        "Chestnut aroma, fresh and sweet aftertaste";

    public static final BigDecimal PRICE = new BigDecimal("100.00");


    public static Tea getGaba() {
        return Tea.builder()
            .name(GABA_NAME)
            .originCountry(TAIWAN)
            .originRegion(ALISHAN)
            .type(TeaType.OOLONG)
            .notes(GABA_NOTES)
            .price(PRICE)
            .build();
    }

    public static Tea getPuer() {
        return Tea.builder()
            .name(PUER_NAME)
            .originCountry(CHINA)
            .originRegion(YUNNAN)
            .type(TeaType.PUER)
            .notes(PUER_NOTES)
            .price(PRICE)
            .build();
    }

    public static Tea getLongjing() {
        return Tea.builder()
            .name(LONGJING_NAME)
            .originCountry(CHINA)
            .originRegion(ZHEJIANG)
            .type(TeaType.GREEN)
            .notes(LONGJING_NOTES)
            .price(PRICE)
            .build();
    }

    public static TeaRequest getGabaRequest() {
        return new TeaRequest(
            GABA_NAME,
            TAIWAN,
            ALISHAN,
            TeaType.OOLONG,
            GABA_NOTES,
            PRICE
        );
    }
}
