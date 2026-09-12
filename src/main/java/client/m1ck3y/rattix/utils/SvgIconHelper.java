package client.m1ck3y.rattix.utils;

import client.m1ck3y.rattix.modules.manager.Category;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgIconHelper {

    private static final Map<String, Integer> TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, SvgData> BUILTIN_SVGS = new HashMap<>();

    public static class SvgData {
        public final String pathData;
        public final double translateY;
        public final double scaleY;

        public SvgData(String pathData, double translateY, double scaleY) {
            this.pathData = pathData;
            this.translateY = translateY;
            this.scaleY = scaleY;
        }
    }

    static {
        // Built-in category SVG path fallbacks
        BUILTIN_SVGS.put("combat", new SvgData(
                "M72 819Q65 817 59.0 811.5Q53 806 50.5 800.5Q48 795 47.5 789.0Q47 783 48 763Q48 740 54 682V681Q57 648 59 638Q61 623 66 613Q71 606 84 589Q99 570 123 547Q152 520 234 447Q330 363 337 356Q338 355 310.5 324.0Q283 293 282.0 293.0Q281 293 260 314Q242 331 232.5 336.5Q223 342 209 344Q201 345 196.5 344.0Q192 343 185 340Q169 333 153.0 318.5Q137 304 129 289Q124 279 124 265Q124 256 125.5 251.0Q127 246 132 236Q140 221 161 198L183 175L100 92L87 98Q74 105 63.5 105.0Q53 105 43.0 99.5Q33 94 24.0 81.0Q15 68 13 56Q11 48 15.5 40.0Q20 32 38 13Q51 -2 94 -46Q137 -90 150 -102Q167 -118 176 -122Q182 -125 195 -125H196Q206 -125 212.5 -122.0Q219 -119 227 -111Q241 -97 243.0 -80.5Q245 -64 234 -48L228 -40L239 -30Q242 -27 258 -11L315 44L336 23Q357 3 368.0 -6.0Q379 -15 386 -17Q401 -22 421.5 -11.5Q442 -1 461 22Q474 38 478.5 47.5Q483 57 482 68Q481 81 474.5 92.0Q468 103 450 121L431 141L452 162L487 194Q498 203 501.0 205.0Q504 207 506 205Q520 193 547.0 168.5Q574 144 574 143L553 119Q535 100 530.0 90.0Q525 80 525.0 64.5Q525 49 530.5 38.0Q536 27 551.0 13.0Q566 -1 579.0 -7.5Q592 -14 606.0 -14.0Q620 -14 629.0 -9.0Q638 -4 662 19Q672 28 680.5 35.0Q689 42 690 42L733 2L762 -27Q771 -35 772.5 -38.0Q774 -41 773 -43Q760 -61 760.5 -79.0Q761 -97 775 -111Q784 -119 792.0 -122.5Q800 -126 811 -126Q819 -126 823.0 -125.0Q827 -124 832 -120Q841 -115 913.5 -41.5Q986 32 988.0 39.0Q990 46 990.0 56.5Q990 67 986.5 74.0Q983 81 973.5 89.5Q964 98 957 101Q935 112 912 93L906 90L863 132L821 175L846 201Q869 226 874.5 235.0Q880 244 880.0 259.0Q880 274 873.5 287.0Q867 300 851.5 315.5Q836 331 824.5 337.0Q813 343 799.5 343.0Q786 343 775.5 337.5Q765 332 745 313L723 292L708 308Q702 314 687 329L664 353L911 574Q923 585 932.0 597.5Q941 610 944.0 620.0Q947 630 954 709Q958 760 959 777Q960 791 958 797Q953 814 937 819Q928 821 808 810Q769 806 755.5 802.0Q742 798 728 785Q724 782 690 744Q653 704 611 656Q504 536 502 536L468 573Q427 618 388 662Q276 788 267 794Q252 803 231.0 807.0Q210 811 147 815Q89 819 85 820Q76 820 72 819ZM152 760Q158 759 171 758Q203 755 212 754Q234 752 236 750Q241 748 267 718Q333 643 407.5 559.5Q482 476 485 474Q496 467 509.5 470.5Q523 474 534 487Q586 547 677 649Q765 747 768 749Q776 754 821 756Q839 758 872 761L904 763Q905 763 898 701Q895 666 893 653Q891 638 888.0 631.5Q885 625 877 617Q867 607 852 595Q811 563 654 418Q624 390 615 380Q603 368 602.0 360.5Q601 353 606 342Q609 335 616.5 326.0Q624 317 655 285L684 254L650 220Q617 185 615.0 185.0Q613 185 570 223Q524 263 519 267Q503 277 487 268Q480 265 436.0 224.5Q392 184 391.0 184.0Q390 184 355 219L320 254L360 299Q385 326 392 334Q401 345 402.0 350.5Q403 356 399.5 365.0Q396 374 379.0 391.0Q362 408 312.5 451.0Q263 494 195 555Q124 618 121 622Q115 628 112.0 643.5Q109 659 106 694Q104 733 102 752L101 766ZM815 276Q826 266 826 261Q826 258 819.0 250.0Q812 242 792 221L758 187V177Q758 165 767.0 153.0Q776 141 811 107Q860 58 870 49Q886 31 894 30H896Q903 28 904 29Q910 32 901 23L828 -51V-40Q828 -29 824.0 -21.0Q820 -13 806 3Q794 16 760.5 49.5Q727 83 715.0 94.0Q703 105 698 106Q690 108 684.5 106.5Q679 105 670.5 98.0Q662 91 643 73L640 70Q620 51 614.0 46.0Q608 41 604.0 41.0Q600 41 595.0 44.5Q590 48 584.0 56.0Q578 64 578.0 67.0Q578 70 633 125L743 233Q798 288 800.5 288.0Q803 288 815 276ZM211 282Q217 278 321.0 174.5Q425 71 426 69Q428 62 418.0 51.5Q408 41 400 41Q397 41 389.5 47.5Q382 54 363 73Q329 105 327 106Q322 108 314.0 107.5Q306 107 302.0 105.5Q298 104 234 40Q193 0 181.5 -12.5Q170 -25 169 -30L168 -32Q167 -37 166 -38L154 -27Q139 -13 125 1L85 39L98 40Q111 40 119.5 43.5Q128 47 143 60Q155 71 188 103Q242 157 243 162Q248 174 242.5 186.0Q237 198 216 219Q192 243 186.0 251.0Q180 259 181.0 263.0Q182 267 191.0 276.5Q200 286 202.5 286.0Q205 286 211 282Z",
                850, -1
        ));

        BUILTIN_SVGS.put("movement", new SvgData(
                "M707 788Q689 784 672.5 771.5Q656 759 648 743Q644 734 643.0 729.0Q642 724 642.0 708.5Q642 693 643.0 688.0Q644 683 648 674Q660 647 687 635Q696 630 701.5 629.0Q707 628 721.0 628.0Q735 628 740.0 629.0Q745 630 754 635Q781 647 794 674V675Q798 684 799.0 689.0Q800 694 800.0 708.5Q800 723 799.0 728.0Q798 733 794 742V743Q789 753 778.5 764.0Q768 775 758 780Q752 783 738.5 786.5Q725 790 720 789ZM424 663Q423 663 405 650L346 611Q293 576 282.0 567.0Q271 558 267 551Q259 536 263.5 519.5Q268 503 282 495Q299 487 313 491Q322 493 384.0 535.0Q446 577 449.5 577.0Q453 577 490.5 569.0Q528 561 529 560L513 535Q493 505 473 476Q435 418 423 400Q407 374 402.5 361.5Q398 349 398 335Q398 313 412.0 296.0Q426 279 458 259Q482 245 535.0 208.5Q588 172 588 170L545 71Q514 2 507.5 -14.0Q501 -30 501 -38Q501 -56 512.5 -69.5Q524 -83 541.0 -87.0Q558 -91 574 -82Q585 -76 590 -70Q592 -67 633 22L649 56L686 140Q697 166 700 175Q702 181 702 189Q702 201 698.5 209.5Q695 218 687 225Q681 231 619 273Q560 313 556 317Q555 319 599 392L645 464L647 457Q658 431 672.0 403.0Q686 375 690.0 370.5Q694 366 700 362Q705 360 779.0 350.0Q853 340 866 340Q882 340 894.0 352.5Q906 365 906 382Q906 395 899.0 406.0Q892 417 881 421Q879 422 845 426L812 431L748 439L690 567Q688 571 651.0 597.0Q614 623 602 629Q597 631 520.0 648.0Q443 665 437 664Q427 664 424 663ZM343 245 319 212 222 211Q153 210 139.5 209.5Q126 209 120 206Q103 197 97.0 179.5Q91 162 97.5 145.0Q104 128 121 119L131 114H245Q327 114 344.0 114.5Q361 115 366 117Q376 121 384.0 130.5Q392 140 412 169L438 207L417 223Q377 255 370 271L366 278Z",
                850, -1
        ));

        BUILTIN_SVGS.put("visual", new SvgData(
                "M455 622Q323 611 209.0 545.5Q95 480 18 370L5 351L10 343Q21 323 44.5 295.5Q68 268 93 243Q205 133 360 93Q468 67 580.0 82.0Q692 97 788 152Q842 184 890.5 227.5Q939 271 976 322Q989 339 991 345L995 352L980 373Q904 480 792.5 545.0Q681 610 551 622Q531 623 503 623Q469 623 455 622ZM541 553Q612 547 679 523Q716 509 757.5 485.0Q799 461 828 436Q844 423 868.5 398.0Q893 373 902 361L911 350L899 336Q887 321 862.0 296.0Q837 271 822 259Q721 179 586 153Q566 149 554.0 148.5Q542 148 500.0 148.0Q458 148 446.0 148.5Q434 149 414 153Q279 179 178 259Q163 271 138.0 296.0Q113 321 101 336L90 350L98 361Q109 375 135.0 401.0Q161 427 177 440Q208 465 247.0 487.5Q286 510 321 523Q373 542 430.5 549.5Q488 557 541 553ZM468 484Q397 464 371 396V395Q366 384 365.0 377.0Q364 370 364.0 350.0Q364 330 365.0 323.0Q366 316 371 305V304Q394 244 454 221H455Q466 216 473.0 215.0Q480 214 500.0 214.0Q520 214 527.0 215.0Q534 216 546 221H547Q608 244 630 304V305Q634 316 635.0 323.0Q636 330 636 350Q636 372 635.0 378.5Q634 385 630 397Q605 458 546 480Q535 484 528.0 485.0Q521 486 504 486Q480 487 468 484Z",
                850, -1
        ));

        BUILTIN_SVGS.put("player", new SvgData(
                "M437 798Q323 783 231.0 717.0Q139 651 87 547Q56 484 45.5 412.5Q35 341 47 270Q57 213 82.5 157.0Q108 101 145 58Q199 -8 272.5 -48.5Q346 -89 432 -103Q455 -106 495.5 -106.0Q536 -106 559 -103Q670 -86 759.5 -21.5Q849 43 899 141Q938 217 946.5 301.5Q955 386 932.5 468.5Q910 551 859 619Q803 694 724.0 740.0Q645 786 552 798Q531 801 494.5 801.0Q458 801 437 798ZM551 724Q596 717 640.5 699.0Q685 681 722 653Q737 642 762.5 617.0Q788 592 800 576Q859 498 872 399Q876 374 874.5 335.5Q873 297 868 272Q859 227 840.0 185.5Q821 144 793 111Q782 96 768.0 81.0Q754 66 752.5 68.0Q751 70 743 92Q721 162 667 216Q638 246 607.5 263.0Q577 280 541 288Q512 294 479.0 291.5Q446 289 418 279Q359 257 312.5 205.0Q266 153 245 85L239 67Q238 65 224.0 79.5Q210 94 201 106Q168 147 146.5 195.0Q125 243 118 295Q116 314 115.5 346.5Q115 379 117 396Q124 449 145.5 498.0Q167 547 200 588Q211 602 232.0 622.0Q253 642 267 653Q329 698 402.0 716.5Q475 735 551 724ZM471 609Q441 602 413 576Q400 563 392.5 551.0Q385 539 379 522Q364 480 372.0 437.5Q380 395 409 364Q421 352 432.5 344.5Q444 337 459 331Q468 328 474.0 327.0Q480 326 494 326Q515 326 528.5 330.0Q542 334 558 345Q578 359 593.0 380.5Q608 402 615 428Q620 447 620.0 470.0Q620 493 614 511Q603 548 580.5 572.5Q558 597 526 607Q515 610 498.5 610.5Q482 611 471 609Z",
                850, -1
        ));

        BUILTIN_SVGS.put("other", new SvgData(
                "M461 748Q395 742 333.0 713.0Q271 684 222 637Q198 615 176.5 585.0Q155 555 141 525Q107 455 101.5 377.0Q96 299 120 225Q150 135 217.5 67.5Q285 0 375 -30Q437 -50 500.5 -50.0Q564 -50 625 -30Q715 0 782.5 67.5Q850 135 880 225Q900 287 900.0 350.5Q900 414 880 475Q851 563 787.5 628.0Q724 693 638.5 725.0Q553 757 461 748ZM561 695Q623 684 678.0 651.5Q733 619 771 571Q826 503 843 419Q847 402 847.5 392.0Q848 382 848.0 350.5Q848 319 847.5 308.5Q847 298 843 281Q825 196 771 130Q734 82 682.5 51.0Q631 20 569 7Q552 3 542.0 2.5Q532 2 500.5 2.0Q469 2 458.5 2.5Q448 3 431 7Q324 30 249.5 106.0Q175 182 155 290Q151 312 152 352Q152 382 153 392Q153 403 157 419Q175 504 229 571Q271 623 329.5 655.5Q388 688 457 698Q473 700 509.0 699.0Q545 698 561 695ZM339 399Q321 394 311.0 381.5Q301 369 301 350Q301 338 304.5 329.5Q308 321 317 314Q334 298 355.5 301.0Q377 304 390.5 321.0Q404 338 399 361Q394 381 376.0 392.0Q358 403 339 399ZM360 373Q367 370 371.0 363.5Q375 357 375 350Q375 340 367.0 332.5Q359 325 349 325Q343 325 336.5 330.0Q330 335 328 341Q323 350 327.5 359.5Q332 369 342 373Q347 375 351.0 375.0Q355 375 360 373ZM490 399Q481 398 472.0 391.0Q463 384 458 376Q446 355 453.0 334.5Q460 314 480.0 304.5Q500 295 521.0 305.0Q542 315 548 338Q554 358 540 379Q532 390 517.5 396.0Q503 402 490 399ZM512 372Q517 369 521.0 362.5Q525 356 525 350Q525 341 517.0 333.0Q509 325 500 325Q494 325 488.5 328.0Q483 331 479.0 337.5Q475 344 475 350Q475 359 483.0 367.0Q491 375 500 375Q506 375 512 372ZM640 399Q618 394 607.5 375.5Q597 357 602.0 337.0Q607 317 627 306Q641 299 656.0 301.0Q671 303 683 314Q692 321 695.5 329.5Q699 338 699.0 349.5Q699 361 695.5 369.5Q692 378 684.0 386.0Q676 394 663.0 398.0Q650 402 640 399ZM663 371Q671 366 673.5 355.5Q676 345 671 337Q663 324 648.0 325.0Q633 326 627.5 339.5Q622 353 629 363Q634 372 644.5 374.0Q655 376 663 371Z",
                850, -1
        ));

        BUILTIN_SVGS.put("theme", new SvgData(
                "M576 320C576 320.9 576 321.8 576 322.7C575.6 359.2 542.4 384 505.9 384L408 384C381.5 384 360 405.5 360 432C360 435.4 360.4 438.7 361 441.9C363.1 452.1 367.5 461.9 371.8 471.8C377.9 485.6 383.9 499.3 383.9 513.8C383.9 545.6 362.3 574.5 330.5 575.8C327 575.9 323.5 576 319.9 576C178.5 576 63.9 461.4 63.9 320C63.9 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320zM192 352C192 334.3 177.7 320 160 320C142.3 320 128 334.3 128 352C128 369.7 142.3 384 160 384C177.7 384 192 369.7 192 352zM192 256C209.7 256 224 241.7 224 224C224 206.3 209.7 192 192 192C174.3 192 160 206.3 160 224C160 241.7 174.3 256 192 256zM352 160C352 142.3 337.7 128 320 128C302.3 128 288 142.3 288 160C288 177.7 302.3 192 320 192C337.7 192 352 177.7 352 160zM448 256C465.7 256 480 241.7 480 224C480 206.3 465.7 192 448 192C430.3 192 416 206.3 416 224C416 241.7 430.3 256 448 256z",
                0, 1
        ));

        BUILTIN_SVGS.put("ghost", new SvgData(
                "M463 829Q432 824 412.0 818.0Q392 812 371 801Q317 775 277.5 730.5Q238 686 218 630Q209 604 205.5 584.0Q202 564 201 524L199 476Q197 474 184 478Q157 484 132.5 472.0Q108 460 96 435Q92 427 91.5 422.0Q91 417 91 403Q91 385 94.5 374.0Q98 363 107.0 351.5Q116 340 128.0 332.5Q140 325 163 316L169 313Q191 305 191.5 304.0Q192 303 184.0 285.0Q176 267 171 258Q153 228 130.0 210.5Q107 193 66 179Q38 169 34 167Q27 163 19.0 154.5Q11 146 6 138Q3 132 2.0 127.0Q1 122 1.0 112.0Q1 102 2.0 97.0Q3 92 6 84Q16 63 39.5 50.0Q63 37 102 29L121 25Q125 24 128 10Q136 -25 159 -41Q169 -48 175.5 -50.0Q182 -52 197 -53Q208 -53 244 -53H304L316 -59Q334 -69 356 -81Q407 -110 436.5 -120.5Q466 -131 500.0 -131.0Q534 -131 563.0 -121.0Q592 -111 644 -81Q667 -69 684 -59L696 -53H756Q792 -53 803 -53Q818 -52 824.5 -50.0Q831 -48 841 -41Q864 -25 872 10Q875 24 879 25L898 29Q937 37 960.5 50.0Q984 63 994 84Q997 92 998.0 97.0Q999 102 999.0 112.0Q999 122 998.0 127.0Q997 132 994 138Q989 146 981.0 154.5Q973 163 966 167Q964 169 943 176L934 179Q892 193 869.0 210.5Q846 228 829 259Q824 267 816.0 285.0Q808 303 808.5 304.0Q809 305 831 313L837 316Q860 325 872.0 332.5Q884 340 893.5 351.5Q903 363 906.0 374.0Q909 385 909 403Q909 417 908.5 422.0Q908 427 904 435Q892 460 867.5 472.0Q843 484 816 478Q803 474 801 476L799 524Q798 567 793 591Q774 679 711.0 742.5Q648 806 559 826Q548 828 510.0 829.5Q472 831 463 829ZM553 772Q624 755 674.0 705.0Q724 655 740 585Q743 570 744.0 556.5Q745 543 746 505Q748 447 750 438Q752 433 756.5 427.5Q761 422 766.0 419.0Q771 416 780.0 416.5Q789 417 807.0 421.5Q825 426 834.5 426.0Q844 426 850.5 418.5Q857 411 857.0 401.0Q857 391 849 382Q846 378 838.0 374.0Q830 370 805.5 360.5Q781 351 770.5 345.0Q760 339 756.0 333.0Q752 327 752 316Q752 302 764.0 272.0Q776 242 790 220Q811 187 840.5 165.5Q870 144 915 129Q947 119 947 112Q947 110 944 105Q939 96 921.0 90.0Q903 84 855 74Q833 70 830 67Q828 66 827 60L823 42Q821 24 818.5 16.0Q816 8 811.5 4.0Q807 0 797.0 -0.5Q787 -1 734 0Q707 1 694 -1Q679 -3 661 -11Q648 -17 613 -37Q585 -52 572.0 -58.5Q559 -65 544 -70Q531 -75 524.5 -76.0Q518 -77 500.5 -77.0Q483 -77 476.5 -76.0Q470 -75 458 -71L456 -70Q441 -65 428.0 -58.5Q415 -52 387 -37Q352 -17 339 -11Q321 -3 306 -1Q293 1 266 0Q213 -1 203.0 -0.5Q193 0 190 3Q185 8 182.0 16.0Q179 24 177 42L173 60Q172 66 170 67Q167 70 145 74Q99 84 80.0 90.5Q61 97 56 105Q53 110 53 112Q53 119 86 129Q130 144 159.5 165.5Q189 187 210 220Q224 242 236.0 272.0Q248 302 248 316Q248 327 244.0 333.0Q240 339 229.5 345.0Q219 351 194 361Q162 374 153.5 380.0Q145 386 143.5 398.0Q142 410 148.5 418.0Q155 426 167 426Q176 426 193.5 421.5Q211 417 220.0 416.5Q229 416 234.0 419.0Q239 422 243.5 427.5Q248 433 250 438Q252 447 254 505Q256 552 258.0 569.0Q260 586 267 608Q272 622 281.5 641.5Q291 661 299 673Q328 712 370.5 739.5Q413 767 457 774Q473 776 475 777H507Q539 775 553 772Z",
                850, -1
        ));
    }

    public static String getCategoryKey(Category category) {
        if (category == null) return null;
        switch (category) {
            case COMBAT:
            case LEGIT:
                return "combat";
            case MOVEMENT:
                return "movement";
            case RENDER:
            case HUD:
                return "visual";
            case PLAYER:
            case LATENCY:
                return "player";
            case WORLD:
            case MISC:
            case FUN:
                return "other";
            case THEME:
                return "theme";
            default:
                return "other";
        }
    }

    public static int getTextureId(String name) {
        if (name == null) return 0;
        String key = name.toLowerCase();
        if (TEXTURE_CACHE.containsKey(key)) {
            return TEXTURE_CACHE.get(key);
        }

        int texId = createTextureForIcon(key);
        TEXTURE_CACHE.put(key, texId);
        return texId;
    }

    private static int createTextureForIcon(String name) {
        SvgData data = loadSvgData(name);
        if (data == null || data.pathData == null || data.pathData.isEmpty()) {
            return 0;
        }

        try {
            Path2D.Double path = parseSvgPath(data.pathData);
            Shape transformed = path;
            if (data.translateY != 0 || data.scaleY != 1) {
                AffineTransform tx = new AffineTransform();
                tx.translate(0, data.translateY);
                tx.scale(1.0, data.scaleY);
                transformed = tx.createTransformedShape(path);
            }

            Rectangle2D bounds = transformed.getBounds2D();
            if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
                return 0;
            }

            int imgSize = 256;
            double margin = 20.0;
            double targetSize = imgSize - (margin * 2.0);
            double maxDim = Math.max(bounds.getWidth(), bounds.getHeight());
            double scale = targetSize / maxDim;

            double cx = bounds.getCenterX();
            double cy = bounds.getCenterY();

            AffineTransform fitTx = new AffineTransform();
            fitTx.translate(imgSize / 2.0, imgSize / 2.0);
            fitTx.scale(scale, scale);
            fitTx.translate(-cx, -cy);

            Shape finalShape = fitTx.createTransformedShape(transformed);

            BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fill(finalShape);
            g2d.dispose();

            int[] pixels = new int[imgSize * imgSize];
            img.getRGB(0, 0, imgSize, imgSize, pixels, 0, imgSize);

            ByteBuffer buffer = ByteBuffer.allocateDirect(imgSize * imgSize * 4);
            for (int y = 0; y < imgSize; y++) {
                for (int x = 0; x < imgSize; x++) {
                    int pixel = pixels[y * imgSize + x];
                    int a = (pixel >> 24) & 0xFF;
                    buffer.put((byte) 255);
                    buffer.put((byte) 255);
                    buffer.put((byte) 255);
                    buffer.put((byte) a);
                }
            }
            buffer.flip();

            int texId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, imgSize, imgSize, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            return texId;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static SvgData loadSvgData(String name) {
        try (InputStream is = SvgIconHelper.class.getResourceAsStream("/assets/rattix/icons/" + name + ".svg")) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(" ");
                }
                String content = sb.toString();

                double transY = 0;
                double scaleY = 1;
                Matcher gMatcher = Pattern.compile("transform=[\"']translate\\(([^,]+),([^\\)]+)\\)\\s+scale\\(([^,]+),([^\\)]+)\\)[\"']").matcher(content);
                if (gMatcher.find()) {
                    transY = Double.parseDouble(gMatcher.group(2).trim());
                    scaleY = Double.parseDouble(gMatcher.group(4).trim());
                }

                Matcher pathMatcher = Pattern.compile("d=[\"']([^\"']+)[\"']").matcher(content);
                if (pathMatcher.find()) {
                    return new SvgData(pathMatcher.group(1), transY, scaleY);
                }
            }
        } catch (Exception ignored) {
        }

        return BUILTIN_SVGS.get(name);
    }

    public static void drawIcon(String name, float x, float y, float size, int color) {
        int texId = getTextureId(name);
        if (texId <= 0) return;

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.bindTexture(texId);
        GlStateManager.color(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + size, 0.0D).tex(0.0D, 1.0D).endVertex();
        wr.pos(x + size, y + size, 0.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(x + size, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static Path2D.Double parseSvgPath(String d) {
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        if (d == null || d.trim().isEmpty()) return path;

        List<String> tokens = tokenize(d);
        double curX = 0, curY = 0;
        double startX = 0, startY = 0;
        double lastCtrlX = 0, lastCtrlY = 0;
        char lastCmd = 0;

        int tIdx = 0;
        char cmd = 0;

        while (tIdx < tokens.size()) {
            String tok = tokens.get(tIdx);
            if (tok.length() == 1 && isCommand(tok.charAt(0))) {
                cmd = tok.charAt(0);
                tIdx++;
            } else if (cmd == 0) {
                tIdx++;
                continue;
            }

            switch (cmd) {
                case 'M':
                case 'm': {
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'm') {
                        curX += x;
                        curY += y;
                    } else {
                        curX = x;
                        curY = y;
                    }
                    path.moveTo(curX, curY);
                    startX = curX;
                    startY = curY;
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    cmd = (cmd == 'm') ? 'l' : 'L';
                    break;
                }
                case 'L':
                case 'l': {
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'l') {
                        curX += x;
                        curY += y;
                    } else {
                        curX = x;
                        curY = y;
                    }
                    path.lineTo(curX, curY);
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'H':
                case 'h': {
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'h') {
                        curX += x;
                    } else {
                        curX = x;
                    }
                    path.lineTo(curX, curY);
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'V':
                case 'v': {
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'v') {
                        curY += y;
                    } else {
                        curY = y;
                    }
                    path.lineTo(curX, curY);
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'C':
                case 'c': {
                    double x1 = Double.parseDouble(tokens.get(tIdx++));
                    double y1 = Double.parseDouble(tokens.get(tIdx++));
                    double x2 = Double.parseDouble(tokens.get(tIdx++));
                    double y2 = Double.parseDouble(tokens.get(tIdx++));
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'c') {
                        x1 += curX; y1 += curY;
                        x2 += curX; y2 += curY;
                        x += curX; y += curY;
                    }
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lastCtrlX = x2;
                    lastCtrlY = y2;
                    curX = x;
                    curY = y;
                    break;
                }
                case 'S':
                case 's': {
                    double x2 = Double.parseDouble(tokens.get(tIdx++));
                    double y2 = Double.parseDouble(tokens.get(tIdx++));
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 's') {
                        x2 += curX; y2 += curY;
                        x += curX; y += curY;
                    }
                    double x1 = curX;
                    double y1 = curY;
                    if (lastCmd == 'C' || lastCmd == 'c' || lastCmd == 'S' || lastCmd == 's') {
                        x1 = 2 * curX - lastCtrlX;
                        y1 = 2 * curY - lastCtrlY;
                    }
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lastCtrlX = x2;
                    lastCtrlY = y2;
                    curX = x;
                    curY = y;
                    break;
                }
                case 'Q':
                case 'q': {
                    double x1 = Double.parseDouble(tokens.get(tIdx++));
                    double y1 = Double.parseDouble(tokens.get(tIdx++));
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 'q') {
                        x1 += curX; y1 += curY;
                        x += curX; y += curY;
                    }
                    path.quadTo(x1, y1, x, y);
                    lastCtrlX = x1;
                    lastCtrlY = y1;
                    curX = x;
                    curY = y;
                    break;
                }
                case 'T':
                case 't': {
                    double x = Double.parseDouble(tokens.get(tIdx++));
                    double y = Double.parseDouble(tokens.get(tIdx++));
                    if (cmd == 't') {
                        x += curX; y += curY;
                    }
                    double x1 = curX;
                    double y1 = curY;
                    if (lastCmd == 'Q' || lastCmd == 'q' || lastCmd == 'T' || lastCmd == 't') {
                        x1 = 2 * curX - lastCtrlX;
                        y1 = 2 * curY - lastCtrlY;
                    }
                    path.quadTo(x1, y1, x, y);
                    lastCtrlX = x1;
                    lastCtrlY = y1;
                    curX = x;
                    curY = y;
                    break;
                }
                case 'Z':
                case 'z': {
                    path.closePath();
                    curX = startX;
                    curY = startY;
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                default:
                    tIdx++;
                    break;
            }
            lastCmd = cmd;
        }

        return path;
    }

    private static boolean isCommand(char c) {
        return "MmLlHhVvCcSsQqTtAaZz".indexOf(c) >= 0;
    }

    private static List<String> tokenize(String d) {
        List<String> tokens = new ArrayList<>();
        int len = d.length();
        int i = 0;
        while (i < len) {
            char c = d.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (isCommand(c)) {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }
            if (c == '+' || c == '-' || c == '.' || Character.isDigit(c)) {
                int start = i;
                boolean hasDot = (c == '.');
                boolean hasExp = false;
                i++;
                while (i < len) {
                    char ch = d.charAt(i);
                    if (Character.isDigit(ch)) {
                        i++;
                    } else if (ch == '.' && !hasDot && !hasExp) {
                        hasDot = true;
                        i++;
                    } else if ((ch == 'e' || ch == 'E') && !hasExp) {
                        hasExp = true;
                        i++;
                        if (i < len && (d.charAt(i) == '+' || d.charAt(i) == '-')) {
                            i++;
                        }
                    } else {
                        break;
                    }
                }
                tokens.add(d.substring(start, i));
                continue;
            }
            i++;
        }
        return tokens;
    }
}
