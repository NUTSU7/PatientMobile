package com.semanticsoft.patientmobile.feature.laborator.dpa.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDocHeaderBg
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.TermsText
import com.semanticsoft.patientmobile.core.ui.theme.icons.DocumentLinesIcon

private sealed interface DpaBlock {
    data class Paragraph(val text: AnnotatedString) : DpaBlock
    data class IndentedItem(val text: AnnotatedString) : DpaBlock
}

private data class DpaSection(
    val title: String,
    val blocks: List<DpaBlock>
)

private fun paragraph(vararg parts: Pair<String, Boolean>): AnnotatedString = buildAnnotatedString {
    parts.forEach { (text, bold) ->
        withStyle(SpanStyle(fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)) {
            append(text)
        }
    }
}

private val dpaSections = listOf(
    DpaSection(
        title = "1. Obiectul Acordului și Rolul Părților",
        blocks = listOf(
            DpaBlock.Paragraph(
                paragraph(
                    "Prezentul Acord de Prelucrare a Datelor cu Caracter Personal („DPA”) completează Termenii și Condițiile Generale ale serviciului Patient și guvernează relațiile tehnico-juridice dintre " to false,
                    "Laboratorul Medical Invitro Chișinău" to true,
                    " (denumit în continuare „Operator”) și " to false,
                    "Patient SRL" to true,
                    " (denumit în continuare „Persoană Împuternicită de Operator” sau „Procesator”)." to false
                )
            ),
            DpaBlock.Paragraph(
                paragraph(
                    "Obiectul constă în facilitarea transmiterii digitale securizate a rezultatelor de analize medicale către pacienți, generarea de rapoarte descriptive și vizualizarea grafică interpretativă autorizată prin intermediul platformei LabLens." to false
                )
            )
        )
    ),
    DpaSection(
        title = "2. Categorii de Date Prelucrate",
        blocks = listOf(
            DpaBlock.Paragraph(
                paragraph(
                    "În cadrul executării serviciului digital, Procesatorul manipulează exclusiv seturile minimale strict necesare scopului diagnostic comunicat:" to false
                )
            ),
            DpaBlock.IndentedItem(
                paragraph(
                    "Date de identificare medicală:" to true,
                    " Număr dosar de laborator (ID intern atribuit de Operator), ID probă, data recoltării și data validării clinice." to false
                )
            ),
            DpaBlock.IndentedItem(
                paragraph(
                    "Date de contact criptate:" to true,
                    " Număr de telefon și/sau adresă de e-mail destinate strict transmiterii notificării securizate cu linkul de acces OTP." to false
                )
            ),
            DpaBlock.IndentedItem(
                paragraph(
                    "Date speciale privind sănătatea:" to true,
                    " Parametrii biochimici, hematologici și imunologici rezultați, valorile de referință ale echipamentului și mențiunile clinice asociate." to false
                )
            )
        )
    ),
    DpaSection(
        title = "3. Obligațiile Părților privind Confidențialitatea și Anonimizarea",
        blocks = listOf(
            DpaBlock.Paragraph(
                paragraph(
                    "Operatorul garantează că a obținut consimțământul legal valid sau deține un temei legal expres conform Regulamentului (UE) 2016/679 (GDPR) și Legii nr. 133/2011 privind protecția datelor cu caracter personal a Republicii Moldova." to false
                )
            ),
            DpaBlock.Paragraph(
                paragraph("Procesatorul garantează că:" to false)
            ),
            DpaBlock.IndentedItem(
                paragraph(
                    "Nu transferă, nu monetizează și nu cedează dreptul de proprietate asupra datelor medicale încărcate." to false
                )
            ),
            DpaBlock.IndentedItem(
                paragraph(
                    "Personalul autorizat al Procesatorului este legat prin acorduri stricte de confidențialitate sub incidența răspunderii penale." to false
                )
            )
        )
    ),
    DpaSection(
        title = "4. Măsuri Tehnice și Organizatorice de Securitate",
        blocks = listOf(
            DpaBlock.Paragraph(
                paragraph(
                    "În conformitate cu standardele HIPAA și GDPR Art. 32, Procesatorul implementează arhitectura de apărare cibernetică multistrat." to false
                )
            )
        )
    ),
    DpaSection(
        title = "5. Notificarea Breșelor de Securitate",
        blocks = listOf(
            DpaBlock.Paragraph(
                paragraph(
                    "În eventualitatea unui incident de securitate confirmat ce afectează integritatea sau confidențialitatea datelor Operatorului, Procesatorul se obligă să notifice responsabilul cu protecția datelor (DPO) al Operatorului în termen maxim de " to false,
                    "24 de ore" to true,
                    " de la conștientizarea evenimentului." to false
                )
            )
        )
    )
)

@Composable
internal fun DpaDocumentCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusXLarge)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(SurfaceWhite, shape)
            .clip(shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LaboratorDocHeaderBg)
                .padding(horizontal = AppDimens.paddingLarge, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimens.gapSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = DocumentLinesIcon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(width = 11.99.dp, height = 12.66.dp)
                )
                Text(
                    text = "DOCUMENT OFICIAL: DPA-MD-2025-INV",
                    color = TermsText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "Ultima revizuire: 14 Ianuarie 2025",
                color = TermsText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppDimens.paddingXLarge)
        ) {
            dpaSections.forEach { section ->
                DocumentSection(section)
            }
        }
    }
}

@Composable
private fun DocumentSection(section: DpaSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapSmall)
    ) {
        Text(
            text = section.title,
            color = Gray900,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold
        )
        section.blocks.forEach { block ->
            when (block) {
                is DpaBlock.Paragraph -> Text(
                    text = block.text,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                is DpaBlock.IndentedItem -> Text(
                    text = block.text,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(start = AppDimens.paddingLarge)
                )
            }
        }
    }
}
