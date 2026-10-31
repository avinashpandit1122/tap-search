# Tap & Learn — Offline Android PDF Reader & Vocabulary Builder

**Status:** MVP specification · **Platform:** Android · **Connectivity:** 100% offline after installation and any optional offline language-pack import · **Primary stack:** Kotlin, Jetpack Compose

## 1. Product overview
Tap & Learn helps people improve English reading comprehension while reading PDF documents. A reader taps or selects an unfamiliar word to see a plain-English meaning, pronunciation guidance, and examples without leaving the document. The app stores vocabulary locally for revision.

### Product goals
- Read imported PDFs without an account or network connection.
- Look up selected English words in an entirely local dictionary.
- Provide beginner-friendly definitions and usage examples when available in the bundled dataset.
- Save words and review them through flashcards and simple quizzes.
- Keep document content and learning history on the device.

### Non-goals for MVP
- Cloud sync, login, online translation, hosted AI, collaboration, or paid subscriptions.
- Guaranteed OCR of scanned/image-only PDFs.
- Guaranteed explanations for every technical word, phrase, or context-dependent meaning.

## 2. Audience and core use cases
Students, English learners, exam candidates, and readers of educational PDFs. Typical flow: import a PDF → open it → select a word → read definition and example → save word → review later.

## 3. MVP functional requirements
| ID | Feature | Required behavior | Priority |
|---|---|---|---|
| F01 | Onboarding | Explain offline reading, word lookup, and local privacy; skip supported | P0 |
| F02 | Document import | Use Android Storage Access Framework to choose PDFs; validate type, size, access and failures | P0 |
| F03 | Library | Show imported files, last opened time, reading progress; open/remove entry | P0 |
| F04 | PDF reader | Render pages, scroll/zoom, navigate by page, resume position | P0 |
| F05 | Text selection | Select word in text-based PDFs and obtain text with page/position where technically available | P0 |
| F06 | Word lookup | Normalize selected word, query local lexicon, show definition and examples | P0 |
| F07 | Missing word | Display clear “not in offline dictionary” message; allow manual saving | P0 |
| F08 | Saved words | Save/delete words, attach source PDF/page, search and filter | P0 |
| F09 | Flashcards | Show saved word then reveal definition; mark review outcome | P1 |
| F10 | Quiz | Simple multiple-choice practice from saved words with enough known entries | P1 |
| F11 | Search | Search library filenames and saved vocabulary locally | P1 |
| F12 | Settings | Theme, font/display preferences where supported, data controls, about | P1 |
| F13 | Accessibility | TalkBack labels, dynamic text, touch targets, contrast, keyboard navigation where relevant | P0 |
| F14 | Data export | Export saved words to CSV/JSON via SAF | P2 |
| F15 | Offline pronunciation | Optional Android on-device TTS if a compatible voice is installed; graceful fallback | P2 |

**Important PDF implementation constraint:** Rendering a page does not automatically enable word selection. The chosen PDF engine must expose selectable text with character/word bounding boxes or be paired with a tested extraction and coordinate-mapping layer. Validate this before committing to a library.

## 4. Suggested Android screen inventory
**18 primary screens / states** (some may be sheets or dialogs, not full activities):

1. Splash / launch
2. Welcome / onboarding
3. Offline/privacy introduction
4. Document library / home
5. Import document / system file picker entry
6. PDF reader
7. Word selection / quick-action popup
8. Word details bottom sheet
9. Dictionary search
10. Dictionary entry / details
11. Saved vocabulary list
12. Saved word details
13. Flashcard review
14. Quiz setup
15. Quiz question
16. Quiz results
17. Reading history / progress
18. Settings / privacy / about

Additional UI states: first-run empty library, missing dictionary entry, unsupported/corrupt PDF, permissions/access loss, password-protected PDF, no selectable text, loading, and large-file error.

## 5. Core UX flows
### A. First reading session
Launch → onboarding → library → import using system picker → reader → long-press/select word → local lookup sheet → read meaning/example → save → return to exact reading position.

### B. Vocabulary practice
Library → Saved words → flashcards or quiz → feedback → updated local review progress.

### C. Unsupported document
Import/open → inspect PDF → if scanned/no extractable text, display “You can read this PDF, but tap-to-define needs selectable text” → continue reading. OCR is a future optional feature.

## 6. Architecture
```text
Jetpack Compose UI
  ├── Navigation + screen ViewModels (StateFlow)
  ├── PDF reader adapter
  │     ├── PDF rendering engine
  │     └── selectable-text extraction + coordinate mapping
  ├── Dictionary repository
  │     └── bundled SQLite/Room-backed offline lexicon
  ├── Vocabulary repository
  │     └── Room (saved words, reviews, reading state)
  └── Document repository
        └── Storage Access Framework URI permissions / private copies
```

**Architecture decisions:** MVVM + repositories; Kotlin coroutines/Flow; Room for user-generated records; a versioned, compressed or prepackaged local dictionary for definitions. Use dependency injection only if the project warrants it. No `INTERNET` permission is needed for MVP.

### PDF engine selection gate
Compare Android `PdfRenderer` (rendering only; does not by itself provide complete selection/lookup) with a compatible open-source or commercial PDF engine offering extraction, selection, and text bounds. Check licensing, Android support, performance, encrypted files, and offline operation. Build a proof of concept using representative PDFs **before** choosing the final engine.

### Offline dictionary content
Use a legally redistributable English lexicon with clear attribution and verified coverage. Normalize inflections where possible (e.g., plural/tense), support multiple senses when present, and distinguish curated examples from generated ones. **Do not claim AI-generated contextual explanations offline unless an on-device model is actually bundled and benchmarked.** Initial MVP should use deterministic dictionary data, not a hosted model.

## 7. Suggested repository structure
```text
app/src/main/java/com/tapandlearn/
  MainActivity.kt
  core/{database,designsystem,files,util}/
  data/{dictionary,documents,vocabulary,reading}/
  domain/{model,repository,usecase}/
  feature/{onboarding,library,reader,lookup,search,
           vocabulary,flashcards,quiz,history,settings}/
  navigation/
app/src/main/assets/dictionary/
app/src/test/
app/src/androidTest/
docs/{architecture,ux,qa,licenses}/
```

## 8. Data model (conceptual)
| Entity | Important fields |
|---|---|
| Document | id, displayName, uriOrPrivatePath, importedAt, lastOpenedAt, lastPage, pageCount |
| DictionaryEntry | lemma, partOfSpeech, senseId, definition, example, pronunciationOptional |
| SavedWord | id, lemma, chosenSenseId, documentIdOptional, pageOptional, savedAt |
| Review | id, savedWordId, reviewedAt, result, nextDueAtOptional |
| QuizAttempt | id, startedAt, completedAt, totalQuestions, correctAnswers |

Use foreign keys, indices on lemma/documentId, and migration tests. Avoid storing PDF content in Room blobs.

## 9. Performance and compatibility targets
- Target a defined supported Android API range after checking PDF engine compatibility.
- Reader must preserve state on rotation, process recreation, and app backgrounding.
- Load/render pages lazily; cache bounded thumbnails/bitmaps; avoid loading entire PDFs into memory.
- Profile dictionary lookup and reader scrolling on low-/mid-range Android devices.
- Treat large PDFs, low storage, password protection, and malformed documents as explicit error cases.
- Define quantitative latency, APK size, and memory budgets after benchmarking the engine and dictionary corpus.

## 10. Privacy and security
- No account, analytics SDK, remote API, or internet requirement for MVP.
- Prefer system picker and persisted URI access; avoid broad storage permissions.
- Keep imported files private or access them through user-granted URIs; clearly disclose whether imports are copied.
- Provide remove-document, clear-history, and delete-vocabulary controls.
- Never log PDF contents, selected text, or private file paths in production.
- Protect against malicious or malformed PDFs by using maintained libraries and testing failure modes.

## 11. Limitations and trade-offs
1. Text-based PDFs support selection only if the extraction/selection layer works; scanned PDFs need OCR.
2. Complex layouts, multi-column text, ligatures, and embedded fonts can produce incorrect selection.
3. Dictionary coverage is finite; rare, technical, and proper-noun entries may be absent.
4. A single word may have several meanings; without sentence-aware disambiguation the displayed sense may be imperfect.
5. Offline examples and pronunciation depend on licensed bundled data or installed TTS voices.
6. Larger dictionaries increase download/storage size; optional packs add installation complexity.
7. Password-protected or DRM-restricted documents may be unsupported.
8. Local-only progress is lost if app data is deleted unless the user exports/backups it.

## 12. Development roadmap
| Phase | Deliverable | Exit criteria |
|---|---|---|
| 0 — Discovery / spike | PDF engine + dictionary license/coverage evaluation | Tap/select/extract word reliably on test corpus; license approved |
| 1 — Foundation | Compose design system, navigation, Room, SAF import | Import/list/remove documents; offline build works |
| 2 — Reader | Render, zoom, resume, text selection | Selection-to-word mapping passes sample PDF suite |
| 3 — Dictionary | Local index, lookup, meaning sheet, missing-entry state | No network needed; deterministic lookup tests pass |
| 4 — Learning | Save words, flashcards, quiz, progress | Full end-to-end learning flow passes |
| 5 — Hardening | Accessibility, performance, security, crash fixes | QA acceptance and release checklist signed off |
| 6 — Release | Play Store assets, privacy disclosures, staged rollout | Release build tested offline and policy reviewed |

**Future (not MVP):** optional offline OCR, phrase lookup, bilingual packs, advanced spaced repetition, on-device context model, highlights/annotations, export/backup enhancements.

## 13. QA test matrix
| Area | Must test |
|---|---|
| Offline | Airplane mode on first launch after install, import, read, lookup, save, review |
| PDFs | Normal text, multi-column, tables, Unicode, scanned, large, malformed, encrypted, zero-page/edge cases |
| Selection | Single word, punctuation, hyphenation, zoomed page, page boundary, selection cancellation |
| Dictionary | Exact match, case, inflection, unknown word, multiple senses, missing example |
| Storage | URI revoked, file moved/deleted, insufficient space, app restart, migration |
| UI | Dark mode, large fonts, screen sizes, TalkBack, landscape, state restoration |
| Learning | Duplicate save, delete, quiz with too few words, persistence and scoring |
| Privacy | No outbound network calls, no sensitive logging, clear-data actions |

### MVP acceptance criteria
- A user can import a supported text-based PDF and read it without connectivity.
- Selecting a supported word displays an offline definition and example (when available) without losing reading position.
- Saved words persist across app restarts and can be reviewed offline.
- Scanned/unsupported PDFs fail gracefully without false promises of word selection.
- No server, hosted model, login, or network dependency is required for core functionality.
- License notices for bundled dictionary/PDF components are included.

## 14. Team ownership
- **Product:** approve scope, success metrics, accessibility and privacy copy.
- **Android engineering:** PDF spike, Compose UI, storage, dictionary integration, persistence, performance.
- **UX/UI:** reader interaction, selection sheet, empty/error states, accessible design specs.
- **QA:** PDF corpus, device matrix, offline regression, performance and release verification.
- **Content/legal review:** dictionary and PDF library licensing/attribution.

## 15. Engineering resources
- Android Jetpack Compose: https://developer.android.com/compose
- Android PdfRenderer: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer
- Android Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- Android Room: https://developer.android.com/training/data-storage/room
- Android accessibility: https://developer.android.com/guide/topics/ui/accessibility
- Android app architecture: https://developer.android.com/topic/architecture
- Wiktionary data/licensing starting point (review per dataset): https://www.wiktionary.org/

## 16. Open decisions before implementation
- Final app/package name and minimum supported Android version.
- PDF engine license, selectable-text reliability, and cost.
- Dictionary dataset, license, word coverage, example availability, and download size.
- Whether PDFs are copied into private storage or referenced by persisted URI.
- Flashcard scheduling algorithm and whether quiz is included in first release.
- Product analytics policy (default MVP: none) and exact release performance targets.

---
**Document owner:** Tap & Learn product team  
**Version:** 1.0 · **Updated:** 2026-10-27  
**Implementation note:** This README is a product/engineering specification, not a claim that the application is already built.
