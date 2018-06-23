package edu.self.services.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.self.model.WordInfo;
import edu.self.model.user.UserWord;
import edu.self.utils.TextUtils;

public class TextAnalyzerStrict implements TextAnalyzer {
    private static final String[] ignorableSuffixes = TextUtils.getAllCases("'s", "'d", "'ll", "'re", "'ve", "n't").toArray(new String[]{});
    private Set<String> managedWords;

    public TextAnalyzerStrict(Set<String> managedWords) {
        this.managedWords = managedWords;
    }

    @Override
    public Map<String, Integer> getWordOccurrencies(String text) {
        Map<String, Integer> words = new HashMap<String, Integer>();
        Map<String, Integer> nonManagedWords = new HashMap<String, Integer>();
        Map<String, String> aliases = new HashMap<String, String>();
        for (String word : text.split("\\s+")) {
            if (word.length() > 1) {
                String managedWord = fetchManagedWord(word);
                if (managedWord != null) {
                    addWords(words, managedWord);
                    continue;
                }
                String[] managedWords = fetchManagedWords(word);
                if (managedWords != null) {
                    addWords(words, managedWords);
                    continue;
                }
                //word = TextUtils.cutSuffix(TextUtils.trimNonLetters(word), ignorableSuffixes);
                word = TextUtils.trimNonLetters(word);
                String alias = word.toLowerCase();
                addWords(nonManagedWords, alias);
                registerAlias(aliases, alias, word);
            }
        }
        for (String alias : nonManagedWords.keySet()) {
            words.put(aliases.get(alias), nonManagedWords.get(alias));
        }
        return words;
    }

    private void registerAlias(Map<String, String> holder, String alias, String word) {
        if (!holder.containsKey(alias) || TextUtils.upperCount(word) < TextUtils.upperCount(holder.get(alias))) {
            holder.put(alias, word);
        }
    }

    private void addWords(Map<String, Integer> holder, String... words) {
        for (String word : words) {
            if (!word.isEmpty()) {
                holder.put(word, holder.containsKey(word) ? holder.get(word) + 1 : 1);
            }
        }
    }

    private String[] fetchManagedWords(String word) {
        String[] parts = word.split("\\W+");
        if (parts.length > 1) {
            for (int i = 0; i < parts.length; ++i) {
                String managedWord = fetchManagedWord(parts[i]);
                if (managedWord == null) {
                    return null;
                }
                parts[i] = managedWord;
            }
            return parts;
        }
        return null;
    }

    private String fetchManagedWord(String word) {
        word = TextUtils.trimNonLetters(word);
        if (word.isEmpty()) {
            return word;
        }
        String wordReduced = getManagedWord(word);
        if (wordReduced != null) {
            return wordReduced;
        }
        wordReduced = TextUtils.cutSuffix(word, ignorableSuffixes);
        if (!wordReduced.isEmpty() && !wordReduced.equals(word)) {
            wordReduced = getManagedWord(wordReduced);
            if (wordReduced != null) {
                return wordReduced;
            }
        }
        if (TextUtils.isRoman(word)) {
            return "";
        }
        return null;
    }

    private boolean isManagedWord(String word) {
        return managedWords.contains(word);
    }

    private String getManagedWord(String word) {
        if (isManagedWord(word)) {
            return word;
        }
        String wordReduced;
        wordReduced = word.toLowerCase();
        if (isManagedWord(wordReduced)) {
            return wordReduced;
        }
        //if (!word.isEmpty() && Character.isUpperCase(word.charAt(0))){ //TODO: some common nouns are capitalized in words.txt!!! fix it
        wordReduced = TextUtils.capitalize(word);
        if (isManagedWord(wordReduced)) {
            return wordReduced;
        }
        //}
        wordReduced = word.toUpperCase();
        if (isManagedWord(wordReduced)) {
            return wordReduced;
        }
        if (!word.isEmpty() && !Character.isUpperCase(word.charAt(0)) && isManagedWord(TextUtils.capitalize(word))) {
            System.out.println(word);
        }
        return null;
    }

    @Override
    public List<UserWord> getWords(String text) {
        return null;
    }

    @Override
    public Map<String, WordInfo> getWordStatistics(String text) {
        return null;
    }
}
