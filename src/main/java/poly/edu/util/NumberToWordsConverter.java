package poly.edu.util;

public class NumberToWordsConverter {

    private static final String[] units = {
        "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    public static String convert(long number) {
        if (number == 0) {
            return "không đồng";
        }

        String snumber = Long.toString(number);
        String mask = "000000000000";
        String sMask = mask.substring(0, mask.length() - snumber.length()) + snumber;
        int billions = Integer.parseInt(sMask.substring(0, 3));
        int millions = Integer.parseInt(sMask.substring(3, 6));
        int hundredThousands = Integer.parseInt(sMask.substring(6, 9));
        int thousands = Integer.parseInt(sMask.substring(9, 12));

        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1:
                tradBillions = convertLessThanOneThousand(billions) + " tỷ ";
                break;
            default:
                tradBillions = convertLessThanOneThousand(billions) + " tỷ ";
        }
        String result = tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1:
                tradMillions = convertLessThanOneThousand(millions) + " triệu ";
                break;
            default:
                tradMillions = convertLessThanOneThousand(millions) + " triệu ";
        }
        if (billions > 0 && millions == 0 && (hundredThousands > 0 || thousands > 0)) {
            result = result + "không triệu ";
        } else {
            result = result + tradMillions;
        }

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1:
                tradHundredThousands = "một nghìn ";
                break;
            default:
                tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " nghìn ";
        }
        if ((billions > 0 || millions > 0) && hundredThousands == 0 && thousands > 0) {
            result = result + "không nghìn ";
        } else {
            result = result + tradHundredThousands;
        }

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result = result + tradThousand;

        String finalResult = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
        if (finalResult.trim().isEmpty()) {
            return "";
        }
        finalResult = finalResult.substring(0, 1).toUpperCase() + finalResult.substring(1).trim() + " đồng chẵn.";
        return finalResult;
    }

    private static String convertLessThanOneThousand(int number) {
        String soText = "";
        int tram = number / 100;
        int chuc = (number % 100) / 10;
        int donvi = number % 10;

        if (tram > 0) {
            soText += units[tram] + " trăm ";
            if (chuc == 0 && donvi > 0) {
                soText += "lẻ ";
            }
        }
        
        if (chuc > 0) {
            if (chuc == 1) {
                soText += "mười ";
            } else {
                soText += units[chuc] + " mươi ";
            }
        }

        if (donvi > 0) {
            if (chuc == 0 && tram == 0) {
                soText += units[donvi];
            } else if (chuc > 1 && donvi == 1) {
                soText += "mốt";
            } else if (chuc > 0 && donvi == 5) {
                soText += "lăm";
            } else {
                soText += units[donvi];
            }
        }
        return soText.trim();
    }
}
