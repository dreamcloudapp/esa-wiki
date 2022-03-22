mkdir -p $2

./esa.sh --preprocess %1 %2\preprocessed.xml.bz2 %2\titles.xml.bz2 --title-exclusion "^(?!category:)[^:]+:[^ ].+$"

# ./esa.sh --preprocess %1 %2\preprocessed.xml.bz2 %2\titles.xml.bz2 %2\id-titles.txt --title-exclusion "^(?!category:)[^: ]+:[^ ].+$" "^(ad )?[0-9]+s?( bc)?$" "^(january|february|march|april|may|june|july|august|september|october|november|december) [0-9]{1,2}( [0-9]{4})?$ "^category talk:.+$"

# ./esa.sh --write-id-titles %2\preprocessed.xml.bz2 %2\id-titles.txt

# ./esa.sh --count-links-and-terms %2 --min-in-links 1 --min-out-links 1 --min-terms 10 --id-titles %2\id-titles.txt

# ./esa.sh --repeat-content %2\terms.xml.bz2 %2\repeated.xml.bz2 --repeat-title 4 --repeat-link 2

# ./esa.sh --write-rare-words %2\annotated.xml.bz2 %2\rare-words.txt 3 --filter classic ascii lower singular --stop-words en-default --min-word-length 3

# ./esa.sh --index %2\annotated.xml.bz2 --threads 12 --batch-size 1000 --filter classic ascii lower singular stemmer --stemmer-depth 3 --stop-words en-default %2\rare-words.txt --min-word-length 3 --tfidf-mode b:nbn --bm25-delta 0.5

# ./esa.sh --article-stats %2\annotated.xml.bz2 --filter classic ascii lower singular stemmer --stemmer-depth 3 --stop-words en-default