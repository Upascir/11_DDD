# ドメイン設計規約

## 値オブジェクト設計規約

### EntityIdサブクラス
- **コンストラクタ**: 必ず`private`で宣言
- **ファクトリメソッド**: `generate()`と`of(String)`を提供
- **継承**: `EntityId`クラスを継承

### Nameサブクラス  
- **コンストラクタ**: 必ず`private`で宣言
- **ファクトリメソッド**: `of(String)`を提供
- **継承**: `Name`クラスを継承
- **最大文字数**: `getMaxLength()`をオーバーライド

### 複雑な値オブジェクト
- **Address、ContactInfo、BankAccount**: `private`コンストラクタ + ファクトリメソッド
- **ファクトリメソッド**: `of(...)`を提供（複数パラメータ対応）
- **デフォルトコンストラクタ**: `private`で定義し、`UnsupportedOperationException`をスロー
- **継承**: `ValueObject`クラスを継承

## コードレビューチェックポイント

### 新しい値オブジェクト作成時
1. 適切な基底クラスを継承しているか？
2. コンストラクタのアクセス修飾子は適切か？
3. ファクトリメソッドを提供しているか？
4. バリデーションロジックは適切か？

### 使用時
1. `new`演算子ではなくファクトリメソッドを使用しているか？
2. テストでも同様にファクトリメソッドを使用しているか？

## 将来的な改善案

### ArchUnitによる自動チェック
```java
// EntityIdサブクラスのコンストラクタがprivateであることをチェック
@ArchTest
static final ArchRule entityIdSubclassesHavePrivateConstructors = 
    classes().that().areAssignableTo(EntityId.class)
        .and().areNotAssignableFrom(EntityId.class)
        .should().haveOnlyPrivateConstructors();

// Nameサブクラスのコンストラクタがprivateであることをチェック  
@ArchTest
static final ArchRule nameSubclassesHavePrivateConstructors = 
    classes().that().areAssignableTo(Name.class)
        .and().areNotAssignableFrom(Name.class)
        .should().haveOnlyPrivateConstructors();
```

### CheckStyleルール
```xml
<!-- コンストラクタのアクセス修飾子チェック -->
<module name="VisibilityModifier">
    <property name="packageAllowed" value="false"/>
    <property name="protectedAllowed" value="false"/>
    <property name="publicMemberPattern" value="^$"/>
</module>
```