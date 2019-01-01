@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoDataColumn {
    int TINYTEXT = 1;
    int TEXT = 2;
    int MEDIUMTEXT = 3;

    int byteSize() default -1;
    int textType() default TINYTEXT;

}
