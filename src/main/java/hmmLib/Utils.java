package hmmLib;

/**
 * Implementation utilities. �๦�ܵ�ʵ�֣�for what?
 */
class Utils {

    public static int initialHashMapCapacity(int maxElements) {
        // Default load factor of HashMaps is 0.75
        return (int)(maxElements / 0.75) + 1;
    }


}
